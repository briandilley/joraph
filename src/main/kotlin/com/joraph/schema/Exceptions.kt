
package com.joraph.schema

import com.joraph.JoraphException


class MissingPrimaryKeyException(val entityDescriptor: EntityDescriptor<*>)
    : JoraphException("${entityDescriptor.entityClass.name} is missing a primary key")

class PrimaryKeyNullPointerException(val entityClass: Class<*>) :
    JoraphException("Primary key was null for class ${entityClass.name}")

class UnknownEntityDescriptorException(val entityClass: Class<*>) :
    JoraphException("Unknown EntityDescriptor for class ${entityClass.name}")

class UnknownFKException @JvmOverloads constructor(
    val entityClass: Class<*>,
    val fk: ForeignKey<*, *>,
    message: String? = "Unknown FK: ${entityClass.name}.${fk} -> ${fk.foreignEntity.name}") :
    JoraphException(message)
