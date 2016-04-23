package BIDMach

import BIDMach.datasinks.DataSink
import BIDMach.datasources.{DataSource, IteratorSource}
import BIDMach.mixins.Mixin
import BIDMach.models.Model
import BIDMach.updaters.Updater
import BIDMat.MatFunctions._
import BIDMat.SciFunctions._
import BIDMat.{Mat, MatIOtrait}

/**
  * Created by james on 4/6/16.
  */
class MockLearner(
    datasource:DataSource,
    model:Model,
    mixins:Array[Mixin],
    updater:Updater,
    datasink:DataSink,
    opts:Learner.Options = new Learner.Options) extends Learner(datasource, model, mixins, updater, datasink, opts) {

  override def nextPass(iter:Iterator[(AnyRef, MatIOtrait)]): Unit = {
    if (opts.debugMem && ipass > 0) Mat.debugMem = true;
    var lastp = 0f
    if (iter != null && datasource.isInstanceOf[IteratorSource]) {
      datasource.asInstanceOf[IteratorSource].opts.iter = iter;
      datasource.asInstanceOf[IteratorSource].iter = iter;
    }
    datasource.reset
    var istep = 0
    println("pass=%2d" format ipass)
    ipass += 1
  }
}
