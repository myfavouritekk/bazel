// Copyright 2011-2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.buildjar.javac.plugins;

import com.google.devtools.build.buildjar.InvalidCommandLineException;

import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.main.Main.Result;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.PropagatedException;

import java.util.List;

/**
 * An interface for additional static analyses that need access to the javac compiler's AST at
 * specific points in the compilation process. This class provides callbacks after the attribute and
 * flow phases of the javac compilation process. A static analysis may be implemented by subclassing
 * this abstract class and performing the analysis in the callback methods. The analysis may then be
 * registered with the BlazeJavaCompiler to be run during the compilation process. See
 * {@link com.google.devtools.build.buildjar.javac.plugins.dependency.StrictJavaDepsPlugin} for an
 * example.
 */
public abstract class BlazeJavaCompilerPlugin {

  /**
   * Allows plugins to pass errors through javac.Main to BlazeJavacMain and cleanly shut down the
   * compiler.
   */
  public static final class PluginException extends RuntimeException {
    private final Result result;
    private final String message;

    /** The compiler's exit status. */
    public Result getResult() {
      return result;
    }

    /** The message that will be printed to stderr before shutting down. */
    @Override
    public String getMessage() {
      return message;
    }

    private PluginException(Result result, String message) {
      this.result = result;
      this.message = message;
    }
  }

  /**
   * Pass an error through javac.Main to BlazeJavacMain and cleanly shut down the compiler.
   */
  protected static Exception throwError(Result result, String message) {
    // Javac re-throws exceptions wrapped by PropagatedException.
    throw new PropagatedException(new PluginException(result, message));
  }

  protected Context context;
  protected Log log;
  protected JavaCompiler compiler;

  /**
   * Preprocess the command-line flags that were passed to javac. This is called before
   * {@link #init(Context, Log, JavaCompiler)} and {@link #initializeContext(Context)}.
   *
   * @param args The command-line flags that javac was invoked with.
   * @throws InvalidCommandLineException if the arguments are invalid
   * @returns The flags that do not belong to this plugin.
   */
  public List<String> processArgs(List<String> args) throws InvalidCommandLineException {
    return args;
  }

  /**
   * Called after all plugins have processed arguments and can be used to customize the Java
   * compiler context.
   */
  public void initializeContext(Context context) {
    this.context = context;
  }
  
  /**
   * Performs analysis actions after the attribute phase of the javac compiler.
   * The attribute phase performs symbol resolution on the parse tree.
   *
   * @param env The attributed parse tree (after symbol resolution)
   */
  public void postAttribute(Env<AttrContext> env) {}

  /**
   * Performs analysis actions after the flow phase of the javac compiler.
   * The flow phase performs dataflow checks, such as finding unreachable
   * statements.
   *
   * @param env The attributed parse tree (after symbol resolution)
   */
  public void postFlow(Env<AttrContext> env) {}

  /**
   * Performs analysis actions when the compiler is done and is about to wipe
   * clean its internal data structures (such as the symbol table).
   */
  public void finish() {}

  /**
   * Initializes the plugin.  Called by
   * {@link com.google.devtools.build.buildjar.javac.BlazeJavaCompiler}'s constructor.
   *
   * @param context The Context object from the enclosing BlazeJavaCompiler instance
   * @param log The Log object from the enclosing BlazeJavaCompiler instance
   * @param compiler The enclosing BlazeJavaCompiler instance
   */
  public void init(Context context, Log log, JavaCompiler compiler) {
    this.context = context;
    this.log = log;
    this.compiler = compiler;
  }
}
