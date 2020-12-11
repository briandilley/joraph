package com.joraph.loader

import com.joraph.JoraphException

class MissingLoaderArgumentException(d: EntityLoaderDescriptor<*, *, *, *>) : JoraphException(
    "Missing an argument of type ${d.argumentProviderClass?.name ?: "Unknown"}"
            + " for loader configured for entity class ${d.entityClass.name ?: "Unknown"}")

class UnconfiguredLoaderException(missingLoaderForEntityClass: Class<*>) :
    JoraphException("Missing an EntityLoader for entity class ${missingLoaderForEntityClass.name}")
