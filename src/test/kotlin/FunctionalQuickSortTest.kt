import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.text.NumberFormat
import java.util.*

class FunctionalQuickSortTest : FunSpec() {
    private val buff = IntArray(500)
    private val random = Random()
    private val percentFormatter = NumberFormat.getPercentInstance(Locale.getDefault())

    init {

        test("findPivot") {
            val a = intArrayOf(4, 67, 23, 10, 34, 5, 32, 48, 9)
            findPivot(a, 0, a.size) shouldBe 34
        }

        test("pivot") {
            val a = intArrayOf(4, 67, 23, 10, 34, 5, 32, 48, 9)
            pivot(a, findPivot(a, 0, a.size),0, a.size-1 ) shouldBe 6
        }

        test("sort") {
            val a = intArrayOf(4, 67, 23, 10, 34, 5, 32, 48, 9)
            sort(
                buff,
                a,
                0,
                a.size,
                0,
                0,
                0,
                findPivot(a, 0, a.size),
                a.size / 3,
                false
            )

            validateArray(a)
        }


        test("quicksort 0") {
            val a = createArray(1000)
            println("Quick sort took ${functionTimer { a.quickSort() }}ns")
            validateArray(a)
        }

        test("quicksort 1") {
            for(i in 0..10000) {
                val a = createArray(10000)
                a.quickSort()
                validateArray(a)
            }
        }

        test("quicksort 2") {
            val a = createArray(100000)
            a.quickSort()
            validateArray(a)
        }

        test("quicksort 3") {
            val a = createArray(10000000)
            a.quickSort(500000)
            validateArray(a)
        }


        test("compare average to java sort 1000") {
            compareAverageAgainstJava(1000, 10000)
        }

        test("compare average to java sort 10000") {
            compareAverageAgainstJava(10000, 100)
        }

        test("compare average to java sort 100000") {
            compareAverageAgainstJava(100000, 100)
        }

        test("compare average to java sort 1000000") {
            compareAverageAgainstJava(1000000, 10)
        }

        test("compare average to java sort 10000000") {
            compareAverageAgainstJava(10000000, 5)
        }

        test("compare average to java sort 1000000 threaded") {
            compareAverageAgainstJava(1000000, 10, 500000)
        }

        test("compare average to java sort 10000000 threaded") {
            compareAverageAgainstJava(10000000, 5, 500000)
        }

    }

    private fun compareAverageAgainstJava(size: Int, iterations: Int, threadSplitThreshold: Int = 0) {
        val a = intArrayOf(4, 67, 23, 10, 34, 5, 32, 48, 9)
        a.quickSort()

        var totalMyTime: Long = 0
        var totalJavaTime: Long = 0
        for(i in 0..iterations) {
            val a1 = createArray(size)
            val a2 = a1.copyOf()

            totalMyTime += functionTimer { a1.quickSort(threadSplitThreshold) }
            totalJavaTime += functionTimer { Arrays.sort(a2) }
        }
        val difference = 1-(totalMyTime/iterations).toDouble()/(totalJavaTime/iterations).toDouble()
        println("Over $size ${if(threadSplitThreshold != 0) "threaded" else "" } KQ sort took ${totalMyTime/iterations}ns, java took ${totalJavaTime/iterations}ns, difference: ${percentFormatter.format(difference)} ${if(difference > 0) "faster" else "slower"}")
    }

    private fun createArray(size: Int): IntArray {
        val a = IntArray(size)
        for(i in a.indices) {
            a[i] = random.nextInt(999999) + 1
        }
        return a
    }

    private fun validateArray(a: IntArray) {
        for (i in 1 until a.size) {
            if (a[i] < a[i - 1]) {
                throw Exception("$i is out of sequence")
            }
        }
    }
}