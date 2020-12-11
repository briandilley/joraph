package com.joraph.debug

import com.joraph.ObjectGraph

object JoraphDebug {
    private val debugInfo = ThreadLocal<DebugInfo?>()

    @JvmStatic
    fun setThreadDebugInfo(info: DebugInfo?) {
        debugInfo.set(info)
    }

    @JvmStatic
    fun clearThreadDebugInfo() {
        debugInfo.set(null)
    }

    @JvmStatic
    fun startDebug() {
        debugInfo.set(DebugInfo())
    }

    @JvmStatic
    fun finishDebug(): DebugInfo? {
        val info = getDebugInfo()
        debugInfo.set(null)
        return info
    }

    @JvmStatic
    fun getDebugInfo(): DebugInfo? {
        return debugInfo.get()
    }

    @JvmStatic
    fun hasDebugInfo(): Boolean {
        return debugInfo.get() != null
    }

    @JvmStatic
    fun addObjectGraph(objectGraph: ObjectGraph) {
        if (!hasDebugInfo()) {
            return
        }
        getDebugInfo()?.addObjectGraph(objectGraph)
    }

    @JvmStatic
    fun addLoaderDebug(loaderDebug: LoaderDebug) {
        if (!hasDebugInfo()) {
            return
        }
        getDebugInfo()!!.addLoaderDebug(loaderDebug)
    }

    @JvmStatic
    fun addLoaderDebug(
        entityClass: Class<*>, loaderTimeMillis: Long, ids: Collection<*>, objects: List<*>) {
        if (!hasDebugInfo()) {
            return
        }
        getDebugInfo()?.addLoaderDebug(entityClass, loaderTimeMillis, ids, objects)
    }
}

