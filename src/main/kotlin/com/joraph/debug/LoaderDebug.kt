package com.joraph.debug

class LoaderDebug {
    var entityClass: Class<*>? = null
    var entityIds: Collection<*>? = null
    var loadedEntities: Collection<*>? = null
    var loaderTimeMillis: Long? = null
    var entityIdCount: Int? = null
    var loadedEntityCount: Int? = null
}
