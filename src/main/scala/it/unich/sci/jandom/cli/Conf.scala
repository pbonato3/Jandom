/**
 * Copyright 2013 Gianluca Amato
 * 
 * This file is part of JANDOM: JVM-based Analyzer for Numerical DOMains
 * JANDOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JANDOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofa
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JANDOM.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unich.sci.jandom.cli

import org.rogach.scallop._
import it.unich.sci.jandom.parameters.WideningScope
import it.unich.sci.jandom.parameters.NarrowingStrategy

/**
 * The class for command line parameters.
 */
class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  def enumConverter(e: Enumeration) = singleArgConverter ( e.withName(_) )
  // we need to factour out common code here
  val wideningScope = opt[WideningScope.Value]("widening", default = Some(WideningScope.default ) )( enumConverter( WideningScope ) )
  val narrowingStrategy = opt[NarrowingStrategy.Value]("narrowing", default = Some(NarrowingStrategy.default) ) ( enumConverter( NarrowingStrategy) )
  val file = opt[String]("input", required=true) 
}