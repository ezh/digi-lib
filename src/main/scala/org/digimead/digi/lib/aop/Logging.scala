/**
 * Digi-Lib - base library for Digi components
 *
 * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * best viewer is *nix console with command line
 * grcat - generic colouriser grcat by Radovan Garabík
 * adb logcat -v threadtime | awk '{for(i=1;i<=NF;i++)if(i!=1&&i!=2)printf$i OFS;print""}' | grcat adb.conf
 * 
 * OR
 * 
 * adb logcat -v threadtime | awk '{
 *  for (i=1; i<=NF; i++) {
 *#    if (i==1) printf$i OFS;printf ""
 *#    if (i==2) printf$i OFS;printf ""
 *    if (i==3) printf(" P%05d",$i);
 *    if (i==4) printf(" T%05d",$i);
 *    if (i==5) printf(" %1s",$i);
 *    if (i==6) printf(" %-24s",$i);
 *    if (i>6) printf$i OFS;printf"";
 *  }
 *  print ""
 *}' | grcat adb.conf
 *
 *
 * example adb.conf
 *regexp=.*exceeds maximum length of 23 characters,.*
 *skip=yes
 *count=more
 *-
 *regexp=^ P\d{5} T\d{5} . @.*$
 *colours=bold
 *count=more
 *-
 *regexp=^ P\d{5} T\d{5} V @.*$
 *colours=bold black
 *count=more
 *
 * we may highlight with colors everything
 * anything like PID/TID/Class/File/Line/Level
 * and than filter or search
 */

package org.digimead.digi.lib.aop

import org.aspectj.lang.Signature
import org.digimead.digi.lib.log.Loggable
import org.digimead.digi.lib.log.{ Logging => LLogging }
import org.digimead.digi.lib.log.Logging.instance2Logging
import org.digimead.digi.lib.log.logger.RichLogger.rich2slf4j

object Logging {
  def enteringMethod(file: String, line: Int, signature: Signature, obj: AnyRef) {
    obj match {
      case logging: Loggable =>
        if (!logging.log.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        logging.log.trace("[L%04d".format(line) + "] enteringMethod " + className + "::" + methodName)
      case other =>
        if (!LLogging.inner.commonLogger.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        LLogging.inner.commonLogger.trace("[L%04d".format(line) + "] enteringMethod " + className + "::" + methodName + " at " + file.takeWhile(_ != '.'))
    }
  }
  def leavingMethod(file: String, line: Int, signature: Signature, obj: AnyRef) {
    obj match {
      case logging: Loggable =>
        if (!logging.log.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        logging.log.trace("[L%04d".format(line) + "] leavingMethod " + className + "::" + methodName)
      case other =>
        if (!LLogging.inner.commonLogger.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        LLogging.inner.commonLogger.trace("[L%04d".format(line) + "] leavingMethod " + className + "::" + methodName + " at " + file.takeWhile(_ != '.'))
    }
  }
  def leavingMethod(file: String, line: Int, signature: Signature, obj: AnyRef, returnValue: Object) {
    obj match {
      case logging: Loggable =>
        if (!logging.log.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        logging.log.trace("[L%04d".format(line) + "] leavingMethod " + className + "::" + methodName + " result [" + returnValue + "]")
      case other =>
        if (!LLogging.inner.commonLogger.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        LLogging.inner.commonLogger.trace("[L%04d".format(line) + "] leavingMethod " + className + "::" + methodName + " at " + file.takeWhile(_ != '.') + " result [" + returnValue + "]")
    }
  }
  def leavingMethodException(file: String, line: Int, signature: Signature, obj: AnyRef, throwable: Exception) {
    obj match {
      case logging: Loggable =>
        if (!logging.log.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        val exceptionMessage = throwable.getMessage();
        logging.log.trace("[L%04d".format(line) + "] leavingMethodException " + className + "::" + methodName + ". Reason: " + exceptionMessage)
      case other =>
        if (!LLogging.inner.commonLogger.isTraceEnabled) return
        val className = signature.getDeclaringType().getSimpleName()
        val methodName = signature.getName()
        val exceptionMessage = throwable.getMessage();
        LLogging.inner.commonLogger.trace("[L%04d".format(line) + "] leavingMethodException " + className + "::" + methodName + " at " + file.takeWhile(_ != '.') + ". Reason: " + exceptionMessage)
    }
  }
  class Basic extends Loggable {
    @log
    def void[T](f: () => T) { f() }
    @log
    def nonVoid[T](f: () => T) = f()
  }
}

/*

Example:

import org.aspectj.lang.reflect.SourceLocation;
import org.digimead.digi.lib.aop.log;
import org.digimead.digi.lib.log.Loggable;

privileged public final aspect AspectLogging {
	public pointcut loggingNonVoid(Loggable obj, log l) : target(obj) && execution(@log !void *(..)) && @annotation(l);

	public pointcut loggingVoid(Loggable obj, log l) : target(obj) && execution(@log void *(..)) && @annotation(l);

	public pointcut logging(Loggable obj, log l) : loggingVoid(obj, l) || loggingNonVoid(obj, l);

	before(final Loggable obj, final log log) : logging(obj, log) {
		SourceLocation location = thisJoinPointStaticPart.getSourceLocation();
		org.digimead.digi.lib.aop.Logging$.MODULE$.enteringMethod(
				location.getFileName(), location.getLine(),
				thisJoinPointStaticPart.getSignature(), obj);
	}

	after(final Loggable obj, final log log) returning(final Object result) : loggingNonVoid(obj, log) {
		SourceLocation location = thisJoinPointStaticPart.getSourceLocation();
		if (log != null && log.result())
			org.digimead.digi.lib.aop.Logging$.MODULE$.leavingMethod(
					location.getFileName(), location.getLine(),
					thisJoinPointStaticPart.getSignature(), obj, result);
		else
			org.digimead.digi.lib.aop.Logging$.MODULE$.leavingMethod(
					location.getFileName(), location.getLine(),
					thisJoinPointStaticPart.getSignature(), obj);
	}

	after(final Loggable obj, final log log) returning() : loggingVoid(obj, log) {
		SourceLocation location = thisJoinPointStaticPart.getSourceLocation();
		org.digimead.digi.lib.aop.Logging$.MODULE$.leavingMethod(
				location.getFileName(), location.getLine(),
				thisJoinPointStaticPart.getSignature(), obj);
	}

	after(final Loggable obj, final log log) throwing(final Exception ex) : logging(obj, log) {
		SourceLocation location = thisJoinPointStaticPart.getSourceLocation();
		org.digimead.digi.lib.aop.Logging$.MODULE$.leavingMethodException(
				location.getFileName(), location.getLine(),
				thisJoinPointStaticPart.getSignature(), obj, ex);
	}
}

*/
