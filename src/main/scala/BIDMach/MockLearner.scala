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
    while (datasource.hasNext) {
      while (paused) Thread.sleep(10)
      val mats = datasource.next;
      here += datasource.opts.batchSize
      bytes += mats.map(Learner.numBytes _).reduce(_+_);
      val dsp = datasource.progress;
      val gprogress = (ipass + dsp)/opts.npasses;

      istep += 1
      if (dsp > lastp + opts.pstep && reslist.length > lasti) {
        val gf = gflop
        lastp = dsp - (dsp % opts.pstep)
        print("%5.2f%%, %s, gf=%5.3f, secs=%3.1f, GB=%4.2f, MB/s=%5.2f" format (
          100f*lastp,
          Learner.scoreSummary(reslist, lasti, reslist.length, opts.cumScore),
          gf._1,
          gf._2,
          bytes*1e-9,
          bytes/gf._2*1e-6))
        if (useGPU) {
          print(", GPUmem=%3.6f" format GPUmem._1)
        }
        println;
        lasti = reslist.length;
      }
    }
    ipass += 1
  }
}
