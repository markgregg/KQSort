
fun functionTimer(runnable: () -> Unit ): Long {
    val start = System.nanoTime()
    runnable()
    return System.nanoTime() - start
}