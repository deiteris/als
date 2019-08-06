# High-level AST module
## Interface
### Core
`org.mulesoft.high.level.Core` class is the entry point to the high-level AST.

`init()` method returning void/unit future must be called before starting the work with the system.

After that one of the following methods should be used to build AST:

`buildModel(unit:BaseUnit,platform:Platform):Future[IProject]`

or 

`buildModel(unit:BaseUnit,fsResolver:IFSProvider):Future[IProject]`

The first method accepts AMF Platform, the second requires a version of file system provider instead.

File system provider contains a number of self-explanatory methods, which are called by the builders to find files and directories:
```
trait IFSProvider {

    def content(fullPath: String): Future[String]

    def dirName(fullPath: String): String

    def existsAsync(path: String): Future[Boolean]

    def resolve(contextPath: String, relativePath: String): Option[String]

    def isDirectory(fullPath: String): Boolean

    def readDirAsync(path: String): Future[Seq[String]]

    def isDirectoryAsync(path: String): Future[Boolean]
}
```

Essentially, both `buildModel` methods require an access to the file system, the only difference is the interface, which represents it.

The result of both methods is an instance of IProject:

```
trait IProject {

    def rootASTUnit: IASTUnit

    def rootPath: String

    def units: Map[String, ASTUnit]

    def types: ITypeCollectionBundle

    def language: Vendor

    def fsProvider: IFSProvider

    def resolve(absBasePath: String, path: String): Option[IASTUnit]

    def resolvePath(path: String, p: String): Option[String]
}
```

Here the most interesting properties are the root AST unit, and the map of all units.

```
trait IASTUnit {

    def universe:IUniverse

    def baseUnit:BaseUnit

    def dependencies: Map[String,DependencyEntry[_ <: IASTUnit]]

    def dependants: Map[String,DependencyEntry[_ <: IASTUnit]]

    def types: ITypeCollection

    def project:IProject

    def rootNode:IHighLevelNode

    def path:String

    def positionsMapper:IPositionsMapper

    def text:String

    def resolve(path:String): Option[IASTUnit]
}
```

For each unit, the root of the AST tree is represented by the `rootNode` property of `IHighLevelNode`, the nodes are described in the next section.

`universe` property points to the global collection of types, `types` property reflects types being used in the unit.
Types are described further below.

## Nodes

Nodes are represented by the `IParseResult` general nodes and the descendants of `IHighLevelNode` and `IAttribute`.
The difference between those two is that attributes have name and value, while general nodes are more complex.

```
trait IParseResult extends IHasExtra {

    def amfNode: AmfObject

    def amfBaseUnit: BaseUnit

    def root: Option[IHighLevelNode]

    def parent: Option[IHighLevelNode]

    def setParent(node: IHighLevelNode): Unit

    def children: Seq[IParseResult]

    def isAttr: Boolean

    def asAttr: Option[IAttribute]

    def isElement: Boolean

    def asElement: Option[IHighLevelNode]

    def isUnknown: Boolean

    def property: Option[IProperty]

    def printDetails(indent: String=""): String

    def printDetails: String = printDetails()

    def astUnit: IASTUnit

    def sourceInfo:ISourceInfo
}
```

The most interesting properties of general node is its link to amd nodes, an ability to check its children, convert it to element (`IHighLevelNode`) or attribute (`IAttribute`), and to check its property pointing to the type system.
 
It is also possible to check node source by using `sourceInfo`

```
trait IHighLevelNode extends IParseResult {
    
    def amfNode: AmfObject

    def localType: Option[ITypeDefinition]

    def definition: ITypeDefinition

    def attribute(n: String): Option[IAttribute]

    def attributeValue(n: String): Option[Any]

    def attributes: Seq[IAttribute]

    def attributes(n: String): Seq[IAttribute]

    def elements: Seq[IHighLevelNode]

    def element(n: String): Option[IHighLevelNode]

    def elements(n: String): Seq[IHighLevelNode]
...
```
High-level nodes can be checked for child elements and attributes.
Also each node has `definition` property pointing to the node definition in terms of type system, and potentially `localType`, which is this node's interpreted type (mostly used for user-defined types, annotations etc)

```
trait IAttribute extends IParseResult {

    def name: String

    def definition: Option[ITypeDefinition]

    def value: Option[Any]
...
```

For attributes name and value are the most important properties.

## Types

Types are represented by `ITypeDefinition` trait.

```
trait ITypeDefinition extends INamedEntity with IHasExtra {
    def key: Option[NamedId]

    def superTypes: Seq[ITypeDefinition]

    def subTypes: Seq[ITypeDefinition]

    def allSubTypes: Seq[ITypeDefinition]

    def allSuperTypes: Seq[ITypeDefinition]

    def properties: Seq[IProperty]

    def facet(n: String): Option[IProperty]

    def allProperties(visited: scala.collection.Map[String,ITypeDefinition]): Seq[IProperty]

    def allProperties: Seq[IProperty]

    def allFacets: Seq[IProperty]

    def allFacets(visited: scala.collection.Map[String,ITypeDefinition]): Seq[IProperty]

    def facets: Seq[IProperty]

    def isValueType: Boolean

    def hasValueTypeInHierarchy: Boolean

    def isArray: Boolean

    def isObject: Boolean

    def hasArrayInHierarchy: Boolean

    def array: Option[IArrayType]

    def arrayInHierarchy: Option[IArrayType]

    def isUnion: Boolean

    def hasUnionInHierarchy: Boolean

    def union: Option[IUnionType]

    def unionInHierarchy: Option[IUnionType]

    def isAnnotationType: Boolean

    def annotationType: Option[IAnnotationType]

    def hasStructure: Boolean

    def isExternal: Boolean

    def hasExternalInHierarchy: Boolean

    def external: Option[IExternalType]

    def externalInHierarchy: Option[IExternalType]

    def isBuiltIn: Boolean

    def universe: IUniverse

    def isAssignableFrom(typeName: String): Boolean

    def property(name: String): Option[IProperty]

    def requiredProperties: Seq[IProperty]

    def getFixedFacets: scala.collection.Map[String,Any]

    def fixedFacets: scala.collection.Map[String,Any]

    def allFixedFacets: scala.collection.Map[String,Any]

    def fixedBuiltInFacets: scala.collection.Map[String,Any]

    def allFixedBuiltInFacets: scala.collection.Map[String,Any]

    def printDetails(indent: String, settings: IPrintDetailsSettings): String

    def printDetails(indent: String): String

    def printDetails: String

    def isGenuineUserDefinedType: Boolean

    def hasGenuineUserDefinedTypeInHierarchy: Boolean

    def genuineUserDefinedTypeInHierarchy: Option[ITypeDefinition]

    def kind: Seq[String]

    def isTopLevel: Boolean

    def isUserDefined: Boolean
}

```

Note that this entity represents not only types declared in `types` section of RAML. Type is a more general abstract. Any annotation is a type. Any type property also has its own type, often anonymous.

Types can be checked for its hierarchy using `superTypes` and `subTypes` properties.

Types has lots of `is...` properties to determine the type's kind.

Types can be requested for facets using `fixedFacets`, `facet` and similar properties.

Types can be checked for being user defined.
 

# RAML Node tables
This links list RAML 1.0 and RAML 0.8 node type tables:

[RAML 1.0](./RAML10Classes.html)

[RAML 0.8](./RAML08Classes.html)

<h2 class="a" id="AnnotationRef">AnnotationRef</h2>
<p>Annotations allow you to attach information to your API</p>

<table>
Instantiation of <a href='#TypeDeclaration'>TypeDeclaration</a>
<h2 class="a"><a name='UsesDeclaration'>UsesDeclaration</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>key</strong>?<em> (key) </em>
</td>
<td>
Name prefix (without dot) used to refer imported declarations
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>value</strong>?
</td>
<td>
Content of the schema
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a" id="AnyType">AnyType</h2>

<table>
<h2 class="a" id="MarkdownString">MarkdownString</h2>
<p><a href="https://help.github.com/articles/github-flavored-markdown/">GitHub Flavored Markdown</a></p>

<table>
<h2 class="a"><a name='ExampleSpec'>ExampleSpec</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this example. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>value</strong>
</td>
<td>
String representation of example
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="owned">
<td>
<strong>strict</strong>?
</td>
<td>
By default, examples are validated against any type declaration. Set this to false to allow examples that need not validate.
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
An alternate, human-friendly name for the example
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the example
</td>
<td>
markdown string
</td>
<tr>
</table>
<h2 class="a"><a name='XMLFacetInfo'>XMLFacetInfo</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>attribute</strong>?
</td>
<td>
If attribute is set to true, a type instance should be serialized as an XML attribute. It can only be true for scalar types.
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>wrapped</strong>?
</td>
<td>
If wrapped is set to true, a type instance should be wrapped in its own XML element. It can not be true for scalar types and it can not be true at the same moment when attribute is true.
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>name</strong>?
</td>
<td>
Allows to override the name of the XML element or XML attribute in it's XML representation.
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>namespace</strong>?
</td>
<td>
Allows to configure the name of the XML namespace.
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>prefix</strong>?
</td>
<td>
Allows to configure the prefix which will be used during serialization to XML.
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a" id="AnnotationTarget">AnnotationTarget</h2>
<p>Elements to which this Annotation can be applied (enum)</p>

<table>
<h2 class="a"><a name='TypeDeclaration'>TypeDeclaration</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="owned">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="owned">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="owned">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="owned">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="owned">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="owned">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
</table>
<h2 class="a"><a name='ArrayTypeDeclaration'>ArrayTypeDeclaration</a></h2>
requires type=array extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#ArrayTypeDeclaration'>ArrayTypeDeclaration</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>uniqueItems</strong>?
</td>
<td>
Should items in array be unique
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>items</strong>?
</td>
<td>
Array component type.
</td>
<td>
Inline type declaration or type name.
</td>
<tr>
<tr class="owned">
<td>
<strong>minItems</strong>?
</td>
<td>
Minimum amount of items in array
</td>
<td>
integer ( >= 0 ). Defaults to 0
</td>
<tr>
<tr class="owned">
<td>
<strong>maxItems</strong>?
</td>
<td>
Maximum amount of items in array
</td>
<td>
integer ( >= 0 ). Defaults to undefined.
</td>
<tr>
</table>
<h2 class="a"><a name='UnionTypeDeclaration'>UnionTypeDeclaration</a></h2>
requires type=union extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#UnionTypeDeclaration'>UnionTypeDeclaration</a><h3>Context requirements:</h3><li>locationKind=LocationKind.MODELS</li><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
</table>
<h2 class="a"><a name='ObjectTypeDeclaration'>ObjectTypeDeclaration</a></h2>
requires type=object extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#ObjectTypeDeclaration'>ObjectTypeDeclaration</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>properties</strong>?
</td>
<td>
The properties that instances of this type may or must have.
</td>
<td>
An object whose keys are the properties' names and whose values are property declarations.
</td>
<tr>
<tr class="owned">
<td>
<strong>minProperties</strong>?
</td>
<td>
The minimum number of properties allowed for instances of this type.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>maxProperties</strong>?
</td>
<td>
The maximum number of properties allowed for instances of this type.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>additionalProperties</strong>?
</td>
<td>
A Boolean that indicates if an object instance has additional properties.
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>discriminator</strong>?
</td>
<td>
Type property name to be used as discriminator, or boolean
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>discriminatorValue</strong>?
</td>
<td>
The value of discriminator for the type.
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>enum</strong>?
</td>
<td>

</td>
<td>
AnyType[]
</td>
<tr>
</table>
<h2 class="a"><a name='StringTypeDeclaration'>StringTypeDeclaration</a></h2>
requires type=string extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#StringTypeDeclaration'>StringTypeDeclaration</a><p>Description:Value must be a string</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>pattern</strong>?
</td>
<td>
Regular expression that this string should path
</td>
<td>
regexp
</td>
<tr>
<tr class="owned">
<td>
<strong>minLength</strong>?
</td>
<td>
Minimum length of the string
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>maxLength</strong>?
</td>
<td>
Maximum length of the string
</td>
<td>
NumberType
</td>
<tr>
</table>
<h2 class="a"><a name='BooleanTypeDeclaration'>BooleanTypeDeclaration</a></h2>
requires type=boolean extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#BooleanTypeDeclaration'>BooleanTypeDeclaration</a><p>Description:Value must be a boolean</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>enum</strong>?
</td>
<td>

</td>
<td>
BooleanType[]
</td>
<tr>
</table>
<h2 class="a"><a name='NumberTypeDeclaration'>NumberTypeDeclaration</a></h2>
requires type=number extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#NumberTypeDeclaration'>NumberTypeDeclaration</a><p>Description:Value MUST be a number. Indicate floating point numbers as defined by YAML.</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>minimum</strong>?
</td>
<td>
(Optional, applicable only for parameters of type number or integer) The minimum attribute specifies the parameter's minimum value.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>maximum</strong>?
</td>
<td>
(Optional, applicable only for parameters of type number or integer) The maximum attribute specifies the parameter's maximum value.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>format</strong>?
</td>
<td>
Value format
</td>
<td>
StringType<br>one of: int32, int64, int, long, float, double, int16, int8
</td>
<tr>
<tr class="owned">
<td>
<strong>multipleOf</strong>?
</td>
<td>
A numeric instance is valid against "multipleOf" if the result of the division of the instance by this keyword's value is an integer.
</td>
<td>
NumberType
</td>
<tr>
</table>
<h2 class="a"><a name='IntegerTypeDeclaration'>IntegerTypeDeclaration</a></h2>
requires type=integer extends <a href='#NumberTypeDeclaration'>NumberTypeDeclaration</a>Globally declarates referencable instance of <a href='#IntegerTypeDeclaration'>IntegerTypeDeclaration</a><p>Description:Value MUST be a integer.</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="inherited">
<td>
<strong>minimum</strong>?
</td>
<td>
(Optional, applicable only for parameters of type number or integer) The minimum attribute specifies the parameter's minimum value.
</td>
<td>
NumberType
</td>
<tr>
<tr class="inherited">
<td>
<strong>maximum</strong>?
</td>
<td>
(Optional, applicable only for parameters of type number or integer) The maximum attribute specifies the parameter's maximum value.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>format</strong>?
</td>
<td>
Value format
</td>
<td>
StringType<br>one of: int32, int64, int, long, int16, int8
</td>
<tr>
<tr class="inherited">
<td>
<strong>multipleOf</strong>?
</td>
<td>
A numeric instance is valid against "multipleOf" if the result of the division of the instance by this keyword's value is an integer.
</td>
<td>
NumberType
</td>
<tr>
</table>
<h2 class="a"><a name='DateOnlyTypeDeclaration'>DateOnlyTypeDeclaration</a></h2>
requires type=date-only extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#DateOnlyTypeDeclaration'>DateOnlyTypeDeclaration</a><p>Description:the "full-date" notation of RFC3339, namely yyyy-mm-dd (no implications about time or timezone-offset)</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
</table>
<h2 class="a"><a name='TimeOnlyTypeDeclaration'>TimeOnlyTypeDeclaration</a></h2>
requires type=time-only extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#TimeOnlyTypeDeclaration'>TimeOnlyTypeDeclaration</a><p>Description:the "partial-time" notation of RFC3339, namely hh:mm:ss[.ff...] (no implications about date or timezone-offset)</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
</table>
<h2 class="a"><a name='DateTimeOnlyTypeDeclaration'>DateTimeOnlyTypeDeclaration</a></h2>
requires type=datetime-only extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#DateTimeOnlyTypeDeclaration'>DateTimeOnlyTypeDeclaration</a><p>Description:combined date-only and time-only with a separator of "T", namely yyyy-mm-ddThh:mm:ss[.ff...] (no implications about timezone-offset)</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
</table>
<h2 class="a"><a name='DateTimeTypeDeclaration'>DateTimeTypeDeclaration</a></h2>
requires type=datetime extends <a href='#TypeDeclaration'>TypeDeclaration</a>Globally declarates referencable instance of <a href='#DateTimeTypeDeclaration'>DateTimeTypeDeclaration</a><p>Description:a timestamp, either in the "date-time" notation of RFC3339, if format is omitted or is set to rfc3339, or in the format defined in RFC2616, if format is set to rfc2616.</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>format</strong>?
</td>
<td>
Format used for this date time rfc3339 or rfc2616
</td>
<td>
StringType<br>one of: rfc3339, rfc2616
</td>
<tr>
</table>
<h2 class="a" id="ContentType">ContentType</h2>

<table>
<h2 class="a"><a name='FileTypeDeclaration'>FileTypeDeclaration</a></h2>
requires type=file extends <a href='#TypeDeclaration'>TypeDeclaration</a><p>Description:(Applicable only to Form properties) Value is a file. Client generators SHOULD use this type to handle file uploads correctly.</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this type. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>schema</strong>?
</td>
<td>
Alias for the equivalent "type" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "type" property, as the "schema" alias for that property name may be removed in a future RAML version. The "type" property allows for XML and JSON schemas.
</td>
<td>
Single string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
A base type which the current type extends, or more generally a type expression.
</td>
<td>
string denoting the base type or type expression
</td>
<tr>
<tr class="inherited">
<td>
<strong>example</strong>?
</td>
<td>
An example of this type instance represented as string or yaml map/sequence. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the examples property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>examples</strong>?
</td>
<td>
An example of this type instance represented as string. This can be used, e.g., by documentation generators to generate sample values for an object of this type. Cannot be present if the example property is present.
</td>
<td>
* Valid value for this type<br>* String representing the serialized version of a valid value
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the type
</td>
<td>
markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>xml</strong>?
</td>
<td>

</td>
<td>
XMLFacetInfo
</td>
<tr>
<tr class="inherited">
<td>
<strong>allowedTargets</strong>?
</td>
<td>
Restrictions on where annotations of this type can be applied. If this property is specified, annotations of this type may only be applied on a property corresponding to one of the target names specified as the value of this property.
</td>
<td>
An array, or single, of names allowed target nodes.
</td>
<tr>
<tr class="inherited">
<td>
<strong>isAnnotation</strong>?
</td>
<td>
Whether the type represents annotation
</td>
<td>
BooleanType
</td>
<tr>
<tr class="owned">
<td>
<strong>fileTypes</strong>?
</td>
<td>
A list of valid content-type strings for the file. The file type */* should be a valid value.
</td>
<td>
ContentType[]
</td>
<tr>
<tr class="owned">
<td>
<strong>minLength</strong>?
</td>
<td>
The minLength attribute specifies the parameter value's minimum number of bytes.
</td>
<td>
NumberType
</td>
<tr>
<tr class="owned">
<td>
<strong>maxLength</strong>?
</td>
<td>
The maxLength attribute specifies the parameter value's maximum number of bytes.
</td>
<td>
NumberType
</td>
<tr>
</table>
<h2 class="a"><a name='Response'>Response</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this response. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>headers</strong>?
</td>
<td>
Detailed information about any response headers returned by this method
</td>
<td>
Object whose property names are the response header names and whose values describe the values.
</td>
<tr>
<tr class="owned">
<td>
<strong>body</strong>?
</td>
<td>
The body of the response: a body declaration
</td>
<td>
Object whose properties are either<br>* Media types and whose values are type objects describing the request body for that media type, or<br>* a type object describing the request body for the default media type specified in the root mediaType property.
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the response
</td>
<td>
Markdown string
</td>
<tr>
</table>
<h2 class="a" id="TraitRef">TraitRef</h2>

<table>
Instantiation of <a href='#Trait'>Trait</a>
<h2 class="a" id="SecuritySchemeRef">SecuritySchemeRef</h2>

<table>
Instantiation of <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>
<h2 class="a"><a name='Trait'>Trait</a></h2>
 extends <a href='#MethodBase'>MethodBase</a>Globally declarates referencable instance of <a href='#Trait'>Trait</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryParameters</strong>?
</td>
<td>
An APIs resources MAY be filtered (to return a subset of results) or altered (such as transforming  a response body from JSON to XML format) by the use of query strings. If the resource or its method supports a query string, the query string MUST be defined by the queryParameters property
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>headers</strong>?
</td>
<td>
Headers that allowed at this position
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryString</strong>?
</td>
<td>
Specifies the query string needed by this method. Mutually exclusive with queryParameters.
</td>
<td>
TypeDeclaration
</td>
<tr>
<tr class="inherited">
<td>
<strong>responses</strong>?
</td>
<td>
Information about the expected responses to a request
</td>
<td>
An object whose keys are the HTTP status codes of the responses and whose values describe the responses.
</td>
<tr>
<tr class="inherited">
<td>
<strong>body</strong>?
</td>
<td>
Some method verbs expect the resource to be sent as a request body. For example, to create a resource, the request must include the details of the resource to create. Resources CAN have alternate representations. For example, an API might support both JSON and XML representations. A method's body is defined in the body property as a hashmap, in which the key MUST be a valid media type.
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>protocols</strong>?
</td>
<td>
A method can override the protocols specified in the resource or at the API root, by employing this property.
</td>
<td>
array of strings of value HTTP or HTTPS, or a single string of such kind, case-insensitive
</td>
<tr>
<tr class="inherited">
<td>
<strong>is</strong>?
</td>
<td>
Instantiation of applyed traits
</td>
<td>
TraitRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
securityScheme may also be applied to a resource by using the securedBy key, which is equivalent to applying the securityScheme to all methods that may be declared, explicitly or implicitly, by defining the resourceTypes or traits property for that resource. To indicate that the method may be called without applying any securityScheme, the method may be annotated with the null securityScheme.
</td>
<td>
SecuritySchemeRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>

</td>
<td>
MarkdownString
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the trait display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>name</strong>?<em> (key) </em>
</td>
<td>
Name of the trait
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>usage</strong>?
</td>
<td>
Instructions on how and when the trait should be used.
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a"><a name='Method'>Method</a></h2>
 extends <a href='#MethodBase'>MethodBase</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryParameters</strong>?
</td>
<td>
An APIs resources MAY be filtered (to return a subset of results) or altered (such as transforming  a response body from JSON to XML format) by the use of query strings. If the resource or its method supports a query string, the query string MUST be defined by the queryParameters property
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>headers</strong>?
</td>
<td>
Headers that allowed at this position
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryString</strong>?
</td>
<td>
Specifies the query string needed by this method. Mutually exclusive with queryParameters.
</td>
<td>
TypeDeclaration
</td>
<tr>
<tr class="inherited">
<td>
<strong>responses</strong>?
</td>
<td>
Information about the expected responses to a request
</td>
<td>
An object whose keys are the HTTP status codes of the responses and whose values describe the responses.
</td>
<tr>
<tr class="inherited">
<td>
<strong>body</strong>?
</td>
<td>
Some method verbs expect the resource to be sent as a request body. For example, to create a resource, the request must include the details of the resource to create. Resources CAN have alternate representations. For example, an API might support both JSON and XML representations. A method's body is defined in the body property as a hashmap, in which the key MUST be a valid media type.
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>protocols</strong>?
</td>
<td>
A method can override the protocols specified in the resource or at the API root, by employing this property.
</td>
<td>
array of strings of value HTTP or HTTPS, or a single string of such kind, case-insensitive
</td>
<tr>
<tr class="inherited">
<td>
<strong>is</strong>?
</td>
<td>
Instantiation of applyed traits
</td>
<td>
TraitRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
securityScheme may also be applied to a resource by using the securedBy key, which is equivalent to applying the securityScheme to all methods that may be declared, explicitly or implicitly, by defining the resourceTypes or traits property for that resource. To indicate that the method may be called without applying any securityScheme, the method may be annotated with the null securityScheme.
</td>
<td>
SecuritySchemeRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>

</td>
<td>
MarkdownString
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the method display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a" id="ResourceTypeRef">ResourceTypeRef</h2>

<table>
Instantiation of <a href='#ResourceType'>ResourceType</a>
<h2 class="a"><a name='ResourceType'>ResourceType</a></h2>
 extends <a href='#ResourceBase'>ResourceBase</a>Globally declarates referencable instance of <a href='#ResourceType'>ResourceType</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>methods</strong>?
</td>
<td>
The methods available on this resource.
</td>
<td>
Object describing the method
</td>
<tr>
<tr class="inherited">
<td>
<strong>is</strong>?
</td>
<td>
A list of the traits to apply to all methods declared (implicitly or explicitly) for this resource. Individual methods may override this declaration
</td>
<td>
array, which can contain each of the following elements:<br>* name of unparametrized trait <br>* a key-value pair with trait name as key and a map of trait parameters as value<br>* inline trait declaration <br><br>(or a single element of any above kind)
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
The resource type which this resource inherits.
</td>
<td>
one of the following elements:<br>* name of unparametrized resource type<br>* a key-value pair with resource type name as key and a map of its parameters as value<br>* inline resource type declaration
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>

</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
The security schemes that apply to all methods declared (implicitly or explicitly) for this resource.
</td>
<td>
array of security scheme names or a single security scheme name
</td>
<tr>
<tr class="inherited">
<td>
<strong>uriParameters</strong>?
</td>
<td>
Detailed information about any URI parameters of this resource
</td>
<td>
object whose property names are the URI parameter names and whose values describe the values
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the resource type display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>name</strong>?<em> (key) </em>
</td>
<td>
Name of the resource type
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>usage</strong>?
</td>
<td>
Instructions on how and when the resource type should be used.
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a"><a name='SecuritySchemePart'>SecuritySchemePart</a></h2>
 extends <a href='#Operation'>Operation</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this security scheme part. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryParameters</strong>?
</td>
<td>
An APIs resources MAY be filtered (to return a subset of results) or altered (such as transforming  a response body from JSON to XML format) by the use of query strings. If the resource or its method supports a query string, the query string MUST be defined by the queryParameters property
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>headers</strong>?
</td>
<td>
Headers that allowed at this position
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>queryString</strong>?
</td>
<td>
Specifies the query string needed by this method. Mutually exclusive with queryParameters.
</td>
<td>
TypeDeclaration
</td>
<tr>
<tr class="inherited">
<td>
<strong>responses</strong>?
</td>
<td>
Information about the expected responses to a request
</td>
<td>
An object whose keys are the HTTP status codes of the responses and whose values describe the responses.
</td>
<tr>
</table>
<h2 class="a"><a name='SecuritySchemeSettings'>SecuritySchemeSettings</a></h2>
 extends <a href='#Annotable'>Annotable</a><h3>This node allows any children</h3><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
</table>
<h2 class="a" id="FixedUriString">FixedUriString</h2>
<p>This  type describes fixed uris</p>

<table>
<h2 class="a"><a name='OAuth1SecuritySchemeSettings'>OAuth1SecuritySchemeSettings</a></h2>
 extends <a href='#SecuritySchemeSettings'>SecuritySchemeSettings</a><h3>This node allows any children</h3><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>requestTokenUri</strong>
</td>
<td>
The URI of the Temporary Credential Request endpoint as defined in RFC5849 Section 2.1
</td>
<td>
FixedUriString
</td>
<tr>
<tr class="owned">
<td>
<strong>authorizationUri</strong>
</td>
<td>
The URI of the Resource Owner Authorization endpoint as defined in RFC5849 Section 2.2
</td>
<td>
FixedUriString
</td>
<tr>
<tr class="owned">
<td>
<strong>tokenCredentialsUri</strong>
</td>
<td>
The URI of the Token Request endpoint as defined in RFC5849 Section 2.3
</td>
<td>
FixedUriString
</td>
<tr>
</table>
<h2 class="a"><a name='OAuth2SecuritySchemeSettings'>OAuth2SecuritySchemeSettings</a></h2>
 extends <a href='#SecuritySchemeSettings'>SecuritySchemeSettings</a><h3>This node allows any children</h3><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>accessTokenUri</strong>
</td>
<td>
The URI of the Token Endpoint as defined in RFC6749 Section 3.2. Not required forby implicit grant type.
</td>
<td>
FixedUriString
</td>
<tr>
<tr class="owned">
<td>
<strong>authorizationUri</strong>?
</td>
<td>
The URI of the Authorization Endpoint as defined in RFC6749 Section 3.1. Required forby authorization_code and implicit grant types.
</td>
<td>
FixedUriString
</td>
<tr>
<tr class="owned">
<td>
<strong>authorizationGrants</strong>
</td>
<td>
A list of the Authorization grants supported by the API as defined in RFC6749 Sections 4.1, 4.2, 4.3 and 4.4, can be any of:<br>* authorization_code<br>* password<br>* client_credentials<br>* implicit <br>*  or any absolute url.
</td>
<td>
StringType[]
</td>
<tr>
<tr class="owned">
<td>
<strong>scopes</strong>?
</td>
<td>
A list of scopes supported by the security scheme as defined in RFC6749 Section 3.3
</td>
<td>
StringType[]
</td>
<tr>
</table>
<h2 class="a"><a name='AbstractSecurityScheme'>AbstractSecurityScheme</a></h2>
 extends <a href='#Annotable'>Annotable</a>Globally declarates referencable instance of <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="owned">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>settings</strong>?
</td>
<td>
The settings attribute MAY be used to provide security scheme-specific information. The required attributes vary depending on the type of security scheme is being declared. It describes the minimum set of properties which any processing application MUST provide and validate if it chooses to implement the security scheme. Processing applications MAY choose to recognize other properties for things such as token lifetime, preferred cryptographic algorithms, and more.
</td>
<td>
SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='OAuth2SecurityScheme'>OAuth2SecurityScheme</a></h2>
requires type=OAuth 2.0 extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#OAuth2SecurityScheme'>OAuth2SecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>settings</strong>?
</td>
<td>

</td>
<td>
OAuth2SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='OAuth1SecurityScheme'>OAuth1SecurityScheme</a></h2>
requires type=OAuth 1.0 extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#OAuth1SecurityScheme'>OAuth1SecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>settings</strong>?
</td>
<td>

</td>
<td>
OAuth1SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='PassThroughSecurityScheme'>PassThroughSecurityScheme</a></h2>
requires type=Pass Through extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#PassThroughSecurityScheme'>PassThroughSecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>settings</strong>?
</td>
<td>

</td>
<td>
SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='BasicSecurityScheme'>BasicSecurityScheme</a></h2>
requires type=Basic Authentication extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#BasicSecurityScheme'>BasicSecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>settings</strong>?
</td>
<td>
The settings attribute MAY be used to provide security scheme-specific information. The required attributes vary depending on the type of security scheme is being declared. It describes the minimum set of properties which any processing application MUST provide and validate if it chooses to implement the security scheme. Processing applications MAY choose to recognize other properties for things such as token lifetime, preferred cryptographic algorithms, and more.
</td>
<td>
SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='DigestSecurityScheme'>DigestSecurityScheme</a></h2>
requires type=Digest Authentication extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#DigestSecurityScheme'>DigestSecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>settings</strong>?
</td>
<td>
The settings attribute MAY be used to provide security scheme-specific information. The required attributes vary depending on the type of security scheme is being declared. It describes the minimum set of properties which any processing application MUST provide and validate if it chooses to implement the security scheme. Processing applications MAY choose to recognize other properties for things such as token lifetime, preferred cryptographic algorithms, and more.
</td>
<td>
SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a"><a name='CustomSecurityScheme'>CustomSecurityScheme</a></h2>
requires type=x-{other} extends <a href='#AbstractSecurityScheme'>AbstractSecurityScheme</a>Globally declarates referencable instance of <a href='#CustomSecurityScheme'>CustomSecurityScheme</a><p>Description:Declares globally referable security scheme definition</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>
</td>
<td>
The securitySchemes property MUST be used to specify an API's security mechanisms, including the required settings and the authentication methods that the API supports. one authentication method is allowed if the API supports them.
</td>
<td>
string<br><br>The value MUST be one of<br>* OAuth 1.0,<br>* OAuth 2.0,<br>* BasicSecurityScheme Authentication<br>* DigestSecurityScheme Authentication<br>* Pass Through<br>* x-&lt;other&gt;
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
The description MAY be used to describe a securityScheme.
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>describedBy</strong>?
</td>
<td>
A description of the request components related to Security that are determined by the scheme: the headers, query parameters or responses. As a best practice, even for standard security schemes, API designers SHOULD describe these properties of security schemes. Including the security scheme description completes an API documentation.
</td>
<td>
SecuritySchemePart
</td>
<tr>
<tr class="inherited">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the security scheme display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>settings</strong>?
</td>
<td>
The settings attribute MAY be used to provide security scheme-specific information. The required attributes vary depending on the type of security scheme is being declared. It describes the minimum set of properties which any processing application MUST provide and validate if it chooses to implement the security scheme. Processing applications MAY choose to recognize other properties for things such as token lifetime, preferred cryptographic algorithms, and more.
</td>
<td>
SecuritySchemeSettings
</td>
<tr>
</table>
<h2 class="a" id="FullUriTemplateString">FullUriTemplateString</h2>
<p>This  type describes absolute uri templates</p>

<table>
<h2 class="a" id="MimeType">MimeType</h2>
<p>This sub type of the string represents mime types</p>

<table>
<h2 class="a"><a name='Resource'>Resource</a></h2>
 extends <a href='#ResourceBase'>ResourceBase</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this resource. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>methods</strong>?
</td>
<td>
The methods available on this resource.
</td>
<td>
Object describing the method
</td>
<tr>
<tr class="inherited">
<td>
<strong>is</strong>?
</td>
<td>
A list of the traits to apply to all methods declared (implicitly or explicitly) for this resource. Individual methods may override this declaration
</td>
<td>
array, which can contain each of the following elements:<br>* name of unparametrized trait <br>* a key-value pair with trait name as key and a map of trait parameters as value<br>* inline trait declaration <br><br>(or a single element of any above kind)
</td>
<tr>
<tr class="inherited">
<td>
<strong>type</strong>?
</td>
<td>
The resource type which this resource inherits.
</td>
<td>
one of the following elements:<br>* name of unparametrized resource type<br>* a key-value pair with resource type name as key and a map of its parameters as value<br>* inline resource type declaration
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the resource.
</td>
<td>
Markdown string
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
The security schemes that apply to all methods declared (implicitly or explicitly) for this resource.
</td>
<td>
array of security scheme names or a single security scheme name
</td>
<tr>
<tr class="inherited">
<td>
<strong>uriParameters</strong>?
</td>
<td>
Detailed information about any URI parameters of this resource
</td>
<td>
object whose property names are the URI parameter names and whose values describe the values
</td>
<tr>
<tr class="owned">
<td>
<strong>displayName</strong>?
</td>
<td>
The displayName attribute specifies the resource display name. It is a friendly name used only for  display or documentation purposes. If displayName is not specified, it defaults to the element's key (the name of the property itself).
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>resources</strong>?
</td>
<td>
A nested resource is identified as any property whose name begins with a slash ("/") and is therefore treated as a relative URI.
</td>
<td>
object describing the nested resource
</td>
<tr>
</table>
<h2 class="a"><a name='DocumentationItem'>DocumentationItem</a></h2>
 extends <a href='#Annotable'>Annotable</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Most of RAML model elements may have attached annotations decribing additional meta data about this element
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="owned">
<td>
<strong>title</strong>
</td>
<td>
Title of documentation section
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>content</strong>
</td>
<td>
Content of documentation section
</td>
<td>
MarkdownString
</td>
<tr>
</table>
<h2 class="a"><a name='Api'>Api</a></h2>
 extends <a href='#LibraryBase'>LibraryBase</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="owned">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this API. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>uses</strong>?
</td>
<td>

</td>
<td>
UsesDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>schemas</strong>?
</td>
<td>
Alias for the equivalent "types" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "types" property, as the "schemas" alias for that property name may be removed in a future RAML version. The "types" property allows for XML and JSON schemas.
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>types</strong>?
</td>
<td>
Declarations of (data) types for use within this API.
</td>
<td>
An object whose properties map type names to type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>traits</strong>?
</td>
<td>
Declarations of traits for use within this API.
</td>
<td>
An object whose properties map trait names to trait declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>resourceTypes</strong>?
</td>
<td>
Declarations of resource types for use within this API.
</td>
<td>
An object whose properties map resource type names to resource type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>annotationTypes</strong>?
</td>
<td>
Declarations of annotation types for use by annotations.
</td>
<td>
An object whose properties map annotation type names to annotation type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>securitySchemes</strong>?
</td>
<td>
Declarations of security schemes for use within this API.
</td>
<td>
An object whose properties map security scheme names to security scheme declarations; or an array of such objects
</td>
<tr>
<tr class="owned">
<td>
<strong>title</strong>
</td>
<td>
Short plain-text label for the API
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the API
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="owned">
<td>
<strong>version</strong>?
</td>
<td>
The version of the API, e.g. 'v1'
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>baseUri</strong>?
</td>
<td>
A URI that's to be used as the base of all the resources' URIs. Often used as the base of the URL of each resource, containing the location of the API. Can be a template URI.
</td>
<td>
FullUriTemplateString
</td>
<tr>
<tr class="owned">
<td>
<strong>baseUriParameters</strong>?
</td>
<td>
Named parameters used in the baseUri (template)
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="owned">
<td>
<strong>protocols</strong>?
</td>
<td>
The protocols supported by the API
</td>
<td>
Array of strings, with each being "HTTP" or "HTTPS", case-insensitive
</td>
<tr>
<tr class="owned">
<td>
<strong>mediaType</strong>?
</td>
<td>
The default media type to use for request and response bodies (payloads), e.g. "application/json"
</td>
<td>
Media type string
</td>
<tr>
<tr class="owned">
<td>
<strong>securedBy</strong>?
</td>
<td>
The security schemes that apply to every resource and method in the API
</td>
<td>
SecuritySchemeRef[]
</td>
<tr>
<tr class="owned">
<td>
<strong>resources</strong>?
</td>
<td>
The resources of the API, identified as relative URIs that begin with a slash (/). Every property whose key begins with a slash (/), and is either at the root of the API definition or is the child property of a resource property, is a resource property, e.g.: /users, /{groupId}, etc
</td>
<td>
Resource[]
</td>
<tr>
<tr class="owned">
<td>
<strong>documentation</strong>?
</td>
<td>
Additional overall documentation for the API
</td>
<td>
DocumentationItem[]
</td>
<tr>
</table>
<h2 class="a"><a name='Overlay'>Overlay</a></h2>
 extends <a href='#Api'>Api</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this API. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>uses</strong>?
</td>
<td>

</td>
<td>
UsesDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>schemas</strong>?
</td>
<td>
Alias for the equivalent "types" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "types" property, as the "schemas" alias for that property name may be removed in a future RAML version. The "types" property allows for XML and JSON schemas.
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>types</strong>?
</td>
<td>
Declarations of (data) types for use within this API.
</td>
<td>
An object whose properties map type names to type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>traits</strong>?
</td>
<td>
Declarations of traits for use within this API.
</td>
<td>
An object whose properties map trait names to trait declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>resourceTypes</strong>?
</td>
<td>
Declarations of resource types for use within this API.
</td>
<td>
An object whose properties map resource type names to resource type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>annotationTypes</strong>?
</td>
<td>
Declarations of annotation types for use by annotations.
</td>
<td>
An object whose properties map annotation type names to annotation type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>securitySchemes</strong>?
</td>
<td>
Declarations of security schemes for use within this API.
</td>
<td>
An object whose properties map security scheme names to security scheme declarations; or an array of such objects
</td>
<tr>
<tr class="owned">
<td>
<strong>title</strong>?
</td>
<td>
Short plain-text label for the API
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the API
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>version</strong>?
</td>
<td>
The version of the API, e.g. 'v1'
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>baseUri</strong>?
</td>
<td>
A URI that's to be used as the base of all the resources' URIs. Often used as the base of the URL of each resource, containing the location of the API. Can be a template URI.
</td>
<td>
FullUriTemplateString
</td>
<tr>
<tr class="inherited">
<td>
<strong>baseUriParameters</strong>?
</td>
<td>
Named parameters used in the baseUri (template)
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>protocols</strong>?
</td>
<td>
The protocols supported by the API
</td>
<td>
Array of strings, with each being "HTTP" or "HTTPS", case-insensitive
</td>
<tr>
<tr class="inherited">
<td>
<strong>mediaType</strong>?
</td>
<td>
The default media type to use for request and response bodies (payloads), e.g. "application/json"
</td>
<td>
Media type string
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
The security schemes that apply to every resource and method in the API
</td>
<td>
SecuritySchemeRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>resources</strong>?
</td>
<td>
The resources of the API, identified as relative URIs that begin with a slash (/). Every property whose key begins with a slash (/), and is either at the root of the API definition or is the child property of a resource property, is a resource property, e.g.: /users, /{groupId}, etc
</td>
<td>
Resource[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>documentation</strong>?
</td>
<td>
Additional overall documentation for the API
</td>
<td>
DocumentationItem[]
</td>
<tr>
<tr class="owned">
<td>
<strong>usage</strong>?
</td>
<td>
contains description of why overlay exist
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>extends</strong>
</td>
<td>
Location of a valid RAML API definition (or overlay or extension), the overlay is applied to.
</td>
<td>
StringType
</td>
<tr>
</table>
<h2 class="a"><a name='Extension'>Extension</a></h2>
 extends <a href='#Api'>Api</a><p>Description:Not described yet</p>
<table>
<tr>
<th width="25%">Property</th>
<th width="45%">Description</th>
<th width="30%">Value type</th>
</tr>
<tr class="inherited">
<td>
<strong>annotations</strong>?
</td>
<td>
Annotations to be applied to this API. Annotations are any property whose key begins with "(" and ends with ")" and whose name (the part between the beginning and ending parentheses) is a declared annotation name.
</td>
<td>
A value corresponding to the declared type of this annotation.
</td>
<tr>
<tr class="inherited">
<td>
<strong>uses</strong>?
</td>
<td>

</td>
<td>
UsesDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>schemas</strong>?
</td>
<td>
Alias for the equivalent "types" property, for compatibility with RAML 0.8. Deprecated - API definitions should use the "types" property, as the "schemas" alias for that property name may be removed in a future RAML version. The "types" property allows for XML and JSON schemas.
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>types</strong>?
</td>
<td>
Declarations of (data) types for use within this API.
</td>
<td>
An object whose properties map type names to type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>traits</strong>?
</td>
<td>
Declarations of traits for use within this API.
</td>
<td>
An object whose properties map trait names to trait declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>resourceTypes</strong>?
</td>
<td>
Declarations of resource types for use within this API.
</td>
<td>
An object whose properties map resource type names to resource type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>annotationTypes</strong>?
</td>
<td>
Declarations of annotation types for use by annotations.
</td>
<td>
An object whose properties map annotation type names to annotation type declarations; or an array of such objects
</td>
<tr>
<tr class="inherited">
<td>
<strong>securitySchemes</strong>?
</td>
<td>
Declarations of security schemes for use within this API.
</td>
<td>
An object whose properties map security scheme names to security scheme declarations; or an array of such objects
</td>
<tr>
<tr class="owned">
<td>
<strong>title</strong>?
</td>
<td>
Short plain-text label for the API
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>description</strong>?
</td>
<td>
A longer, human-friendly description of the API
</td>
<td>
MarkdownString
</td>
<tr>
<tr class="inherited">
<td>
<strong>version</strong>?
</td>
<td>
The version of the API, e.g. 'v1'
</td>
<td>
StringType
</td>
<tr>
<tr class="inherited">
<td>
<strong>baseUri</strong>?
</td>
<td>
A URI that's to be used as the base of all the resources' URIs. Often used as the base of the URL of each resource, containing the location of the API. Can be a template URI.
</td>
<td>
FullUriTemplateString
</td>
<tr>
<tr class="inherited">
<td>
<strong>baseUriParameters</strong>?
</td>
<td>
Named parameters used in the baseUri (template)
</td>
<td>
TypeDeclaration[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>protocols</strong>?
</td>
<td>
The protocols supported by the API
</td>
<td>
Array of strings, with each being "HTTP" or "HTTPS", case-insensitive
</td>
<tr>
<tr class="inherited">
<td>
<strong>mediaType</strong>?
</td>
<td>
The default media type to use for request and response bodies (payloads), e.g. "application/json"
</td>
<td>
Media type string
</td>
<tr>
<tr class="inherited">
<td>
<strong>securedBy</strong>?
</td>
<td>
The security schemes that apply to every resource and method in the API
</td>
<td>
SecuritySchemeRef[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>resources</strong>?
</td>
<td>
The resources of the API, identified as relative URIs that begin with a slash (/). Every property whose key begins with a slash (/), and is either at the root of the API definition or is the child property of a resource property, is a resource property, e.g.: /users, /{groupId}, etc
</td>
<td>
Resource[]
</td>
<tr>
<tr class="inherited">
<td>
<strong>documentation</strong>?
</td>
<td>
Additional overall documentation for the API
</td>
<td>
DocumentationItem[]
</td>
<tr>
<tr class="owned">
<td>
<strong>usage</strong>?
</td>
<td>
contains description of why extension exist
</td>
<td>
StringType
</td>
<tr>
<tr class="owned">
<td>
<strong>extends</strong>
</td>
<td>
Location of a valid RAML API definition (or overlay or extension), the extension is applied to
</td>
<td>
StringType
</td>
<tr>
</table>