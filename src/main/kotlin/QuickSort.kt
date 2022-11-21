import kotlinx.coroutines.*

fun IntArray.quickSort() {
    sort(this, 0, size-1)
}

fun sort(a: IntArray, start: Int, end: Int) {
    // start and end are the same we have nothing to sort, so stop
    if (start < end) {
        //partition the array into values lower than the pivot and values higher than the pivot
        val partitionIndex = partition(a, start, end)
        //repeat the process with the lower partition ignoring the pivot as it has been sorted
        sort(a, start, partitionIndex-1)
        //repeat the process with the upper partition ignoring the pivot as it has been sorted
        sort(a, partitionIndex+1, end)
    }
}

fun partition(a: IntArray, start: Int, end: Int): Int {
    //set the partition index to the very start of array (or the section we are sorting)
    var partitionIndex = start
    //take the pivot from the start of the array
    val pivot = a[start]

    //loop through the values of the array, I can ignore the first as that is the pivot
    for (j in start+1..end) {
        //if the current element is equal or less than the pivot then swap with the current element
        if (a[j] <= pivot) {
            swap(a, ++partitionIndex, j)
        }
    }
    //move the pivot to the partition index
    swap(a, start, partitionIndex)
    return partitionIndex
}

fun swap(a: IntArray, from: Int, to: Int) {
    //use a temp variable to swap the two elements
    val temp = a[to]
    a[to] = a[from]
    a[from] = temp
}


fun IntArray.quickSort(threadThreshold: Int) {
    sortParallel(this, 0, size-1, threadThreshold)
}

fun sortParallel(a: IntArray, start: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) {
    if (start < end) {
        val partitionIndex = partition(a, start, end)
        //check if a parallel threshold has been supplied and if the threshold has been exceeded
        if( threadThreshold > 0 && end - start > threadThreshold ) {
            //Check if the array has already split the array
            if( scope == null ) {
                //if so run blocking
                runBlocking {
                    runParallel(a, start, partitionIndex, end, threadThreshold, this)
                }
            } else {
                //otherwise run in the same scope
                scope.launch { runParallel(a, start, partitionIndex, end, threadThreshold, scope) }
            }
        } else {
            sortParallel(a, start, partitionIndex-1, threadThreshold)
            sortParallel(a, partitionIndex+1, end, threadThreshold)
        }
    }
}

suspend fun runParallel(a: IntArray, start: Int, partitionIndex: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) = coroutineScope {
    launch(Dispatchers.Default) {
        sortParallel(a, start, partitionIndex-1, threadThreshold, scope)
    }
    launch(Dispatchers.Default) {
        sortParallel(a, partitionIndex+1, end, threadThreshold, scope)
    }
}