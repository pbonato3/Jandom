/**
 * Copyright 2013 Gianluca Amato <gamato@unich.it>
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

package it.unich.sci.jandom.targets.jvmsoot

import scala.collection.immutable.Stack
import it.unich.sci.jandom.domains.objects.UP
import it.unich.sci.jandom.targets.linearcondition.LinearCond
import soot._
import it.unich.sci.jandom.domains.objects.PairSharingDomain
import it.unich.sci.jandom.domains.objects.ObjectDomain

/**
 * A domain for pair sharing analysis, as described by Secci and Spoto.
 * @author Gianluca Amato <gamato@unich.it>
 */

class SootFramePairSharingDomain(scene: Scene, classAnalysis: ClassReachableAnalysis, roots: IndexedSeq[Local]) extends SootFrameDomain {

  val dom = PairSharingDomain

  val localMap: Map[Local, Int] = roots.zipWithIndex.toMap

  def top(stack: Stack[Type]) = Property(dom.top(roots.size + stack.size), stack)
  def bottom(stack: Stack[Type]) = Property(dom.bottom(roots.size + stack.size), stack)
  def initial = bottom(Stack())

  case class Property(val prop: dom.Property, val stack: Stack[Type]) extends SootFrameProperty[Property] {

    def size = roots.size + stack.size

    def roots = SootFramePairSharingDomain.this.roots

    def classOfVar(i: Int): SootClass = {
      val tpe = if (i < roots.size) roots(i).getType() else stack(i - roots.size)
      assert(tpe.isInstanceOf[RefType])
      tpe.asInstanceOf[RefType].getSootClass()
    }

    val isPossibleFromFieldAssignment = { p: UP[Int] => classAnalysis.mayShare(classOfVar(p._1), classOfVar(p._2)) }

    private def delUntrackedVariable =
      Property(prop.delVariable(size-1), stack.pop)

    private def addUntrackedVariable(tpe: Type) =
      Property(prop.addVariable.assignNull(size), stack.push(NullType.v()))

    def evalConstant(c: Int) = addUntrackedVariable(IntType.v())

    def evalNull =
      Property(prop.addVariable.assignNull(size), stack.push(NullType.v()))

    def evalNew(tpe: Type) =
      Property(prop.addVariable, stack.push(tpe))

    def evalLocal(l: Local) = {
      val v = localMap(l)
      Property(prop.addVariable.assignVariable(size, v), stack.push(l.getType()))
    }

    def evalLength = addUntrackedVariable(IntType.v())

    def evalField(l: Local, f: SootField) = {
      val v = localMap(l)
      Property( prop.addVariable.assignFieldToVariable(size, v, f.getNumber(), isPossibleFromFieldAssignment), stack.push(f.getType()))
    }

    def evalAdd = delUntrackedVariable
    def evalSub = delUntrackedVariable
    def evalMul = delUntrackedVariable
    def evalDiv = delUntrackedVariable
    def evalRem = delUntrackedVariable
    def evalShl = delUntrackedVariable
    def evalShr = delUntrackedVariable
    def evalUshr = delUntrackedVariable
    def evalBinOp = delUntrackedVariable
    def evalNeg = delUntrackedVariable

    def evalGt = delUntrackedVariable
    def evalGe = delUntrackedVariable
    def evalLt = delUntrackedVariable
    def evalLe = delUntrackedVariable
    def evalEq = delUntrackedVariable
    def evalNe = delUntrackedVariable

    def test = {
      val dropped = delUntrackedVariable
      (dropped, dropped)
    }

    def testGt = evalGt.test
    def testGe = evalGe.test
    def testLe = evalLe.test
    def testLt = evalLt.test
    def testEq = evalEq.test
    def testNe = evalNe.test

    def evalLinearForm(lf: Array[Double]) = addUntrackedVariable(DoubleType.v())
    def testLinearCondition(lc: LinearCond) = (this, this)

    def assignLocal(l: Local) = {
      val dst = localMap(l)
      if (l.getType().isInstanceOf[RefType])
        Property(prop.assignVariable(dst, size - 1).delVariable(), stack.pop)
      else
        Property(prop.delVariable(), stack.pop)
    }

    def assignField(l: Local, f: SootField) = {
      val dst = localMap(l)
      Property(prop.assignVariableToField(dst, f.getNumber(), size - 1).delVariable(), stack.pop)
    }

    def mkString(vars: IndexedSeq[String]) =
      prop mkString vars

    def union(that: Property) = {
      assert(stack == that.stack)
      Property(prop union that.prop, stack)
    }

    def intersection(that: Property) = {
      assert(stack == that.stack)
      Property(prop union that.prop, stack)
    }

    def widening(that: Property) = this union that

    def narrowing(that: Property) = this intersection that

    def isEmpty = false

    def tryCompareTo[B >: Property](other: B)(implicit arg0: (B) => PartiallyOrdered[B]): Option[Int] =
      other match {
        case other: Property => prop tryCompareTo other.prop
        case _ => None
      }
  }

}