# JavaCompiler

`JavaCompiler` 是程序中调用Java™ 程序语言编译器的接口。

编译器在编译过程中也许会生存一些诊断信息（diagnostics，例如错误消息）。如果诊断监听器（diagnostic listener）被设置了，诊断信息将会发送给诊断监听器。如果没有设置诊断监听器的划，诊断信息会默认输出到`System.err`里。但是即使设置了监听器，可能某些诊断信息没有适配的`Diagnostic`，这些诊断信息也发送到默认输出里。

编译器工具里被设置了一个标准文件管理器（standard file manager），该文件管理器一般被内建在了工具里。可以调用`getStandardFileManager`获取到该标准文件管理器。

A compiler tool must function with any file manager as long as any additional requirements as detailed in the methods below are met. 

如果没有手动设置文件管理器，那编译器工具将会使用通过`getStandardFileManager`方法获得的标准文件管理器。

实现`JavaCompiler`接口的实例必须符合`The Java™ Language Specification`，而且生产的class文件必须符合`The Java™ Virtual Machine Specification`。这些规范的版本号已经定义在了`Tool`接口里。还有，实现了`SourceVersion.RELEASE_6`以及更高版本的实例必须支持注解处理器（annotation processing）。

编译器依赖于俩个服务`diagnostic listener`和`file manager`。尽管这个包下有很多类和接口为编译器定义类一套API，但是像`DiagnosticListener`, `JavaFileManager`, `FileObject`, 和 `JavaFileObject` 这些接口并不适合直接在应用程序中使用。
这些接口是一般用来为编译器提供一些定制化的需求。

## StandardJavaFileManager

每个编译器都会实现`StandardJavaFileManager`接口提供一个标准文件管理器以便可以操作常规文件。`StandardJavaFileManager`接口还定义了用于从常规文件中创建文件对象的方法。文件管理器主要用于：

* 实现编译器的读写文件功能
* 在多个编译任务间共享

复用文件管理器可能会减轻扫描文件系统以及读取jar文件时的负载。但即使没有减轻负载，一个标准的文件管理器也必须在多个编译任务中顺序使用，例如：

```java
        File[] files1 = ... ; // input for first compilation task
        File[] files2 = ... ; // input for second compilation task

       JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
       StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits1 =
           fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files1));
       compiler.getTask(null, fileManager, null, null, null, compilationUnits1).call();

        Iterable<? extends JavaFileObject> compilationUnits2 =
           fileManager.getJavaFileObjects(files2); // use alternative method
       // reuse the same file manager to allow caching of jar files
       compiler.getTask(null, fileManager, null, null, null, compilationUnits2).call();

       fileManager.close();
```
       
## DiagnosticCollector

批量收集诊断信息，例如

```java
        Iterable<? extends JavaFileObject> compilationUnits = ...;
       JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
       StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
       compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();

       for ( Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())
           System.out.format("Error on line %d in %s%n",
                             diagnostic.getLineNumber(),
                             diagnostic.getSource().toUri());

       fileManager.close();
```

## ForwardingJavaFileManager, ForwardingFileObject, and ForwardingJavaFileObject

Subclassing is not available for overriding the behavior of a standard file manager as it is created by calling a method on a compiler, 
not by invoking a constructor. Instead forwarding (or delegation) should be used. These classes makes it easy to forward most calls to a given file manager or file object while allowing customizing behavior. 
For example, consider how to log all calls to JavaFileManager.flush():

```java
        final Logger logger = ...;
        Iterable<? extends JavaFileObject> compilationUnits = ...;
       JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
       StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
       JavaFileManager fileManager = new ForwardingJavaFileManager(stdFileManager) {
           public void flush() throws IOException {
               logger.entering(StandardJavaFileManager.class.getName(), "flush");
               super.flush();
               logger.exiting(StandardJavaFileManager.class.getName(), "flush");
           }
       };
       compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
```
      
## SimpleJavaFileObject

这个类提供了一个基础的文件对象（file object）实现。
下面的代码实现了如何将 源代码存储在文件对象里的string实例中。

```java
        /**
        * A file object used to represent source coming from a string.
         */
       public class JavaSourceFromString extends SimpleJavaFileObject {
           /**
            * The source code of this "file".
             */
           final String code;

           /**
            * Constructs a new JavaSourceFromString.
            *  @param name the name of the compilation unit represented by this file object
            *  @param code the source code for the compilation unit represented by this file object
             */
           JavaSourceFromString(String name, String code) {
               super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),
                     Kind.SOURCE);
               this.code = code;
           }

            @Override
           public CharSequence getCharContent(boolean ignoreEncodingErrors) {
               return code;
           }
       }
```

# JavaFileManager

文件管理器用来操作Java™ 语言的源文件和编译后的class文件。在当前上下文中，文件代表正常文件和其他数据源的一个抽象表示。

For example, if a file manager manages regular files on a file system, it would most likely have a current/working directory to use as default location when creating or finding files.
 A number of hints can be provided to a file manager as to where to create files. Any file manager might choose to ignore these hints.

当构建新的JavaFileObjects 实例，文件管理器必须知道在哪里创建它们。例如，如果一个文件管理器管理着一个文件系统里面的常规文件，当它创建或者查找文件时，它很可能会有一个默认位置作为工作文件夹使用。可以向文件管理器提供很多参数指定哪里创建文件。但是文件管理器也许会忽略掉这些参数。

Some methods in this interface use class names. Such class names must be given in the Java Virtual Machine internal form of fully qualified class and interface names. 
For convenience '.' and '/' are interchangeable. The internal form is defined in chapter four of The Java™ Virtual Machine Specification.

这个接口当中的某些方法使用了类名。这些类名必须符合Java Virtual Machine全限定类名和接口名的内部表示形式。为了使用方便，`.`和`/`是可互换的。该内部表示形式被定义在了`The Java™ Virtual Machine Specification`第四章。

> 简述：这意味着  `java/lang.package-info`, `java/lang/package-info`, `java.lang.package-info` 这些都是有效的也是都相等的。
>
> Discussion: this means that the names "java/lang.package-info", "java/lang/package-info", "java.lang.package-info", are valid and equivalent. 
> Compare to binary name as defined in The Java™ Language Specification, section 13.1 "The Form of a Binary".


The case of names is significant. All names should be treated as case-sensitive. For example, some file systems have case-insensitive, case-aware file names. 
File objects representing such files should take care to preserve case by using File.getCanonicalFile() or similar means. 
If the system is not case-aware, file objects must use other means to preserve case.

Relative names: some methods in this interface use relative names. A relative name is a non-null, non-empty sequence of path segments separated by '/'. '.' or '..' are invalid path segments. 
A valid relative name must match the "path-rootless" rule of RFC 3986, section 3.3. Informally, this should be true:

```java
  URI.create(relativeName).normalize().getPath().equals(relativeName)
```

All methods in this interface might throw a SecurityException.

An object of this interface is not required to support multi-threaded access, that is, be synchronized. However, it must support concurrent access to different file objects created by this object.

Implementation note: a consequence of this requirement is that a trivial implementation of output to a JarOutputStream is not a sufficient implementation. 
That is, rather than creating a JavaFileObject that returns the JarOutputStream directly, the contents must be cached until closed and then written to the JarOutputStream.

Unless explicitly allowed, all methods in this interface might throw a NullPointerException if given a null argument.

* `close()`
* `flush()`
* `getClassLoader`:
* `getFileForInput`:
* `getFileForOutput`:
* `getJavaFileForInput`:
* `getJavaFileForOutput`:
* `handleOption`:
* `hasLocation`:
* `inferBinaryName`:
* `isSameFile`:
* `list`:

# StandardJavaFileManager


File manager based on java.io.File. A common way to obtain an instance of this class is using getStandardFileManager, for example:

```java
   JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics =
       new  DiagnosticCollector<JavaFileObject>();
   StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);
```

This file manager creates file objects representing regular files, zip file entries, or entries in similar file system based containers. 
Any file object returned from a file manager implementing this interface must observe the following behavior:

* File names need not be canonical.
* For file objects representing regular files
    * the method FileObject.delete() is equivalent to File.delete(),
    * the method FileObject.getLastModified() is equivalent to File.lastModified(),
    * the methods FileObject.getCharContent(boolean), FileObject.openInputStream(), and FileObject.openReader(boolean) must succeed if the following would succeed (ignoring encoding issues):
        `new FileInputStream(new File(fileObject.toUri()))`
    * and the methods FileObject.openOutputStream(), and FileObject.openWriter() must succeed if the following would succeed (ignoring encoding issues):
        `new FileOutputStream(new File(fileObject.toUri()))`
* The URI returned from FileObject.toUri()
    * must be absolute (have a schema), and
    * must have a normalized path component which can be resolved without any process-specific context such as the current directory (file names must be absolute).

According to these rules, the following URIs, for example, are allowed:
* `file:///C:/Documents%20and%20Settings/UncleBob/BobsApp/Test.java`
* `jar:///C:/Documents%20and%20Settings/UncleBob/lib/vendorA.jar!com/vendora/LibraryClass.class`

Whereas these are not (reason in parentheses):
* `file:BobsApp/Test.java (the file name is relative and depend on the current directory)`
* `jar:lib/vendorA.jar!com/vendora/LibraryClass.class (the first half of the path depends on the current directory, whereas the component after ! is legal)`
* `Test.java (this URI depends on the current directory and does not have a schema)`
* `jar:///C:/Documents%20and%20Settings/UncleBob/BobsApp/../lib/vendorA.jar!com/vendora/LibraryClass.class (the path is not normalized)`

