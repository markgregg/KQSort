# An attempt to write sort faster than the JDK

I started this challenge to see if I could write a sort routine faster than the sort algorithm available in the JDK. As I am currently working with Kotlin, that was my language of choice.

## Common sort routines
If you’ve familiar with sorting, you will be aware there are a few standard approaches. If you are not, I have listed a few of the common approaches below.

## Bubble sort
A bubble sort involves looping through the elements of the array from 1 to Max-1. For each element a nested loop is performed (N to Max) in which the current elements is compared to the elements in the inner loop. If any are lower, then they are swapped. The algorithm's performance is O(n2), so it isn’t efficient.

`
fun bubbleSort(a: IntArray) {

    var t: Int? = 0
    while(t != null){
        t = null
        for(i in 0 until a.size-1){
            if(a[i] > a[i+1]){
                t = a[i]
                a[i] = a[i+1]
                a[i + 1] = t
            }
        }
    }
}
`

## Insertion Sort
This insertion sort splits an array in two. One side for the sorted elements and the other for the un-sorted. The sorted side starts with one element and the sort loops from the other side inserting the elements into the correct positions.

`
fun insertionSort(a: IntArray) {

    for (idx in 1 until a.size){
        val comp = a[idx]
        var compIdx = idx
        while(compIdx > 0 && a[compIdx - 1] > comp) {
            a[compIdx] = a[compIdx - 1]
            compIdx--
        }
        a[compIdx] = comp
    }
}
`

## Quick Sort
The quick sort is an approach that involves dividing the array into smaller more manageable chucks. It achieves this by picking an element to pivot around and then moving the higher elements to one side of the pivot and the lower elements to the other. The process is repeated on the two halves of the array, and so on and so on, until the array is sorted.

I will cover the quick sort in more detail below

## Where to start

I figured the best place to start an attempt to match the performance of the JDK sort algorithm is with a quick sort. Based on my own experience with sorting, quick sorts usually give very good results.

## The quick sort implementation

To expose the sort, I decided to use an extension function. Extension functions are an offering from Kotlin that allow you to decorate a class with new functionality. It acts as a rather elegant wrapper and allows me to expose a slick interface and have full access to the array.


`fun IntArray.quickSort() {`

    `sort(this, 0, size-1)`

`}`

## The quick sort
To perform the quick sort, the first thing I need to do is to partition the array. I don’t have a partition function at the moment, but I know the function’s contact, so inserting a placeholder isn’t hard.

`fun sort(a: IntArray, start: Int, end: Int) {`

    `//partition the array into values lower than the pivot and values higher than the pivot`
    `val partitionIndex = partition(a, start, end)`

`}`

## The partition function
The partition function is the part of the quick sort that does most of the wok. Its job is to determine the value to pivot around and then to partition the array either side of the pivot.

There are several ways I can approach the function, but I am going to loop through the array and move the values equal or lower than the pivot to the start. By taking my pivot from the first element, I can skip checking the first element. I will instead start from the second and check all elements up to and including the last. 

If an element is equal or lower than the pivot, I will increment the partition index (initially it is set to the pivot element) and then swap the element current at the partition index with the element I am comparing against.

When the loop is complete, all elements higher than the pivot will be above the partition index and all elements equal or lower will be below. Just to remind you, the pivot was the element at the start. Before exiting the function, I will swap the pivot (from the start), with the value at the current partition index and then I will return the partition index. Note, the pivot value is now in its final position and can be ignored in future.

`fun partition(a: IntArray, start: Int, end: Int): Int {`

    `//set the partition index to the very start of array (or the section we are sorting)`
    `var partitionIndex = start`

    `//take the pivot from the start of the array`
    `val pivot = a[start]`

    `//loop through the values of the array, I can ignore the first as that is the pivot`
    `for (j in start+1..end) {`

        `//if the current element is equal or less than the pivot then swap with the current element`
        `if (a[j] <= pivot) {`

            `swap(a, ++partitionIndex, j)`

        `}`

    `}`

    `//move the pivot to the partition index`
    `swap(a, start, partitionIndex)`

    `return partitionIndex`

`}`

## Back to the sort function
The partition function will return the partition index, which is the dividing point between the two partitions. If you remember, it is also the location of the pivot, which is now in its final position. So, to sort the rest of the array, we just need to call the sort function for the two partitions, minus the partition index. So, for the lower half, the start will be the original start and the end will be the partition index – 1 (to ignore the pivot). The start for the upper half will be the partition index +1 (to avoid the pivot) and the end will be the original end.

`fun sort(a: IntArray, start: Int, end: Int) {`

    `//partition the array into values lower than the pivot and values higher than the pivot`
    `val partitionIndex = partition(a, start, end)`

    `//repeat the process with the lower partition ignoring the pivot as it has been sorted`
    `sort(a, start, partitionIndex-1)`

    `//repeat the process with the upper partition ignoring the pivot as it has been sorted`
    `sort(a, partitionIndex+1, end)`

`}`

## To avoid a stack overflow
Finally, to avoid a stack overflow, I will check that we have elements to sort. I will do that by confirming start is less the end.

`fun sort(a: IntArray, start: Int, end: Int) {`

    `// start and end are the same we have nothing to sort, so stop`
    `if (start < end) {`

        `//partition the array into values lower than the pivot and values higher than the pivot`
        `val partitionIndex = partition(a, start, end)`

        `//repeat the process with the lower partition ignoring the pivot as it has been sorted`
        `sort(a, start, partitionIndex-1)`

        `//repeat the process with the upper partition ignoring the pivot as it has been sorted`
        `sort(a, partitionIndex+1, end)`

    `}`

`}`

## Performance against the JDK sort
Pleasingly, the sort performance is comparable to the JDK sort, although the JDK sort wins on larger arrays. 

Over 1000  KQ sort took 63680ns, java took 73769ns, difference: 14% faster

Over 10000  KQ sort took 1043859ns, java took 805297ns, difference: -30% slower

Over 100000  KQ sort took 10603603ns, java took 11063389ns, difference: 4% faster

Over 1000000  KQ sort took 118214340ns, java took 115771530ns, difference: -2% slower

Over 10000000  KQ sort took 1576546680ns, java took 1419264640ns, difference: -11% slower

## Can I do better?
It is possible to beat the JDK sort. Providing we are on multi core machine, we can sort the partitions in parallel. If you are unaware, Kotlin has a multithreaded offering called coroutines. Coroutines are lightweight virtual threads. In the java world the closest thing is either tasks or Project Loom

The first thing I am going to do is to create a new extension method that accepts a threshold parameter. This will allow me to control at what point the sorting of the array should be parallelised.


`fun IntArray.quickSort(threadThreshold: Int) {`

    `sortParallel(this, 0, size-1, threadThreshold)`

`}`

I will then modify the sort function to check if the current section of the array exceeds the threshold. If it does, I will then call a new function to parallelise the sorting.

`fun sortParallel(a: IntArray, start: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) {`

    `if (start < end) {`

        `val partitionIndex = partition(a, start, end)`

        `//check if a parallel threshold has been supplied and if the threshold has been exceeded`
        `if( threadThreshold > 0 && end - start > threadThreshold ) {`

            `//Check if the array has already split the array`
            `if( scope == null ) {`

                `//if so run blocking`
                `runBlocking {`

                    `runParallel(a, start, partitionIndex, end, threadThreshold, this)`

                `}`

            `} else {`

                `//otherwise run in the same scope`
                `scope.launch { runParallel(a, start, partitionIndex, end, threadThreshold, scope) }`

            `}`

        `} else {`

            `sortParallel(a, start, partitionIndex-1, threadThreshold)`
            `sortParallel(a, partitionIndex+1, end, threadThreshold)`

        `}`

    `}`

`}`

The run parallel function runs the sort for the two partitions in separate threads.

`suspend fun runParallel(a: IntArray, start: Int, partitionIndex: Int, end: Int, threadThreshold: Int, scope: CoroutineScope? = null) = coroutineScope {`

    `launch(Dispatchers.Default) {`

        `sortParallel(a, start, partitionIndex-1, threadThreshold, scope)`

    `}`

    `launch(Dispatchers.Default) {`

        `sortParallel(a, partitionIndex+1, end, threadThreshold, scope)`

    `}`

`}`


## The final results
I kind of expected it would be better and am pleased with the results.

Over 1000000 threaded KQ sort took 74028699ns, java took 115552809ns, difference: 36% faster

Over 10000000 threaded KQ sort took 467783600ns, java took 1482450939ns, difference: 68% faster
