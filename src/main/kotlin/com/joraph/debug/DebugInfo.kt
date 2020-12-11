package com.joraph.debug

import com.joraph.ObjectGraph
import java.util.ArrayList


class DebugInfo {

    private val objectGraphs: MutableSet<ObjectGraph> = LinkedHashSet()
    private val loaderDebugs: MutableList<LoaderDebug> = ArrayList()

    fun addObjectGraph(objectGraph: ObjectGraph) {
        objectGraphs.add(objectGraph)
    }

    fun addLoaderDebug(loaderDebug: LoaderDebug) {
        loaderDebugs.add(loaderDebug)
    }

    fun addLoaderDebug(
        entityClass: Class<*>, loaderTimeMillis: Long, ids: Collection<*>, objects: List<*>) {
        val loaderDebug = LoaderDebug()
        loaderDebug.entityClass = entityClass
        loaderDebug.loadedEntityCount = objects.size
        loaderDebug.loadedEntities = objects
        loaderDebug.loaderTimeMillis = loaderTimeMillis
        loaderDebug.entityIdCount = ids.size
        loaderDebug.entityIds = ids
        loaderDebugs.add(loaderDebug)
    }

    fun getObjectGraphs(): Set<ObjectGraph> {
        return objectGraphs
    }

    fun getLoaderDebugs(): List<LoaderDebug> {
        return loaderDebugs
    }
}
