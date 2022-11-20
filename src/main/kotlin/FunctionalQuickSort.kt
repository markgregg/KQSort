import kotlinx.coroutines.*

private const val chunkSize = 301
private const val bufferSize = 301

fun IntArray.quickSort(threadThreshold: Int = 0) {
    val buff = IntArray(bufferSize)
    divideAndConquer(buff, this, 0, size, threadThreshold)
}

fun divideAndConquer(buff: IntArray, a: IntArray, start: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) {
    val split = pivot(a, findPivot(a, start, end-1), start, end-1)
    if( split == start || split == end) {
        sort(
            if( end - start > bufferSize) IntArray(end - start) else buff,
            a,
            start,
            end,
            start,
            0,
            start,
            findPivot(a, start, end),
            (end - start) / 3,
            false
        )
    } else {
        if( threadThreshold > 0 && end - start > threadThreshold ) {
            if( scope == null ) {
                runBlocking {
                    runParallel(a, start, split, end, threadThreshold, this)
                }
            } else {
                scope.launch { runParallel(a, start, split, end, threadThreshold, scope) }
            }
        } else {
            divideOrSort(buff, a, start, split, threadThreshold, scope)
            divideOrSort(buff, a, split, end, threadThreshold, scope)
        }
    }
}

fun divideOrSort(buff: IntArray, a: IntArray, start: Int, end: Int, threadThreshold: Int, scope: CoroutineScope?) {
    if( end - start > chunkSize) {
        divideAndConquer(buff, a, start, end, threadThreshold, scope)
    } else {
        sort(
            buff,
            a,
            start,
            end,
            start,
            0,
            start,
            findPivot(a, start, end),
            (end - start) / 3,
            false,
        )
    }
}

suspend fun runParallel(a: IntArray, start: Int, split: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) = coroutineScope {
    launch(Dispatchers.Default) {
        divideOrSort(IntArray(bufferSize), a, start, split, threadThreshold, scope)
    }
    launch(Dispatchers.Default) {
        divideOrSort(IntArray(bufferSize), a, split, end, threadThreshold, scope)
    }
}

tailrec fun advanceLeft(a: IntArray, pivot: Int, left: Int, right: Int ): Int {
    return if( left < right && pivot > a[left]) {
        advanceLeft(a, pivot, left+1, right)
    } else {
        left
    }
}
tailrec fun advanceRight(a: IntArray, pivot: Int, left: Int, right: Int): Int {
    return if( left < right && pivot <= a[right]) {
        advanceRight(a, pivot, left, right-1)
    } else {
        right
    }
}

tailrec fun pivot(a: IntArray, pivot: Int, oldLeft: Int, oldRight: Int): Int {
    val left = advanceLeft(a, pivot, oldLeft, oldRight)
    val t = a[left]
    val right = advanceRight(a, pivot, left, oldRight)
    if( left < right ) {
        a[left] = a[right]
        a[right] = t
        return pivot(a, pivot, left, right)
    }
    return left
}

tailrec fun insertIntoArray(a: IntArray, start: Int, pos: Int, element: Int) {
    if( pos > start && element < a[pos-1] ) {
        a[pos] = a[pos-1]
        return insertIntoArray(a, start, pos-1, element)
    }
    a[pos] = element
}

tailrec fun sort(
    buff: IntArray,
    a: IntArray,
    start: Int,
    end: Int,
    oldLeft: Int,
    oldRight: Int,
    pos: Int,
    pivot: Int,
    pivotSwitch: Int,
    switched: Boolean
) {
    var left = oldLeft
    var right = oldRight
    val element = a[pos]
    if( element < pivot ) {
        if( !switched && left > pivotSwitch + right && element > a[left-1] ) {
            return sort(buff, a, start, end, left, right, pos, element, pivotSwitch, true)
        }
        insertIntoArray(a, start, left, element)
        left++
    } else {
        if( !switched && left > pivotSwitch + right && element < buff[0] ) {
            return sort(buff, a, start, end, left, right, pos, element, pivotSwitch, true)
        }
        insertIntoArray(buff, 0, right, element)
        right++
    }
    if( pos < end-1 ) {
        sort(buff, a, start, end, left, right, pos + 1, pivot, pivotSwitch, switched)
    } else {
        copyArray(buff, a, 0, left, right)
     }
}


tailrec fun copyArray(source: IntArray, dest: IntArray, sourceOff: Int, destOff: Int, len: Int) {
    dest[destOff+sourceOff] = source[sourceOff]
    if( sourceOff+1 < len ) copyArray(source, dest, sourceOff+1, destOff, len)
}

internal fun findPivot(a: IntArray, start: Int, end: Int): Int {
    return a[start + (  end - start ) / 2]
}