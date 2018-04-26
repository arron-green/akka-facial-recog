package org.self

import java.io._
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier
import scala.io.Source

sealed trait HaarCascadeModel { val resource: String }

case object Eye extends HaarCascadeModel {
  val resource = "haarcascade/eye.xml"
}

case object EyeTreeEyeGlasses extends HaarCascadeModel {
  val resource = "haarcascade/eye_tree_eyeglasses.xml"
}

case object FrontalCatFace extends HaarCascadeModel {
  val resource = "haarcascade/fontalcatface.xml"
}

case object FrontalCatFaceExtended extends HaarCascadeModel {
  val resource = "haarcascade/fontalcatface_extended.xml"
}

case object FrontalFaceAlt extends HaarCascadeModel {
  val resource = "haarcascade/frontalface_alt.xml"
}

case object FrontalFaceAlt2 extends HaarCascadeModel {
  val resource = "haarcascade/frontalface_alt2.xml"
}

case object FrontalFaceAltTree extends HaarCascadeModel {
  val resource = "haarcascade/frontalface_alt_tree.xml"
}

case object FrontalFaceDefault extends HaarCascadeModel {
  val resource = "haarcascade/frontalface_default.xml"
}

case object FullBody extends HaarCascadeModel {
  val resource = "haarcascade/fullbody.xml"
}

case object Hand extends HaarCascadeModel {
  val resource = "haarcascade/hand.xml"
}

case object LeftEye2Splits extends HaarCascadeModel {
  val resource = "haarcascade/lefteye_2splits.xml"
}

case object LowerBody extends HaarCascadeModel {
  val resource = "haarcascade/lowerbody.xml"
}

case object ProfileFace extends HaarCascadeModel {
  val resource = "haarcascade/profileface.xml"
}

case object RightEye2Splits extends HaarCascadeModel {
  val resource = "haarcascade/righteye_2splits.xml"
}

case object Smile extends HaarCascadeModel {
  val resource = "haarcascade/smile.xml"
}

case object UpperBody extends HaarCascadeModel {
  val resource = "haarcascade/upperbody.xml"
}

case object LeftEye extends HaarCascadeModel {
  val resource = "haarcascade/mcs_lefteye_alt.xml"
}

case object RightEye extends HaarCascadeModel {
  val resource = "haarcascade/mcs_righteye_alt.xml"
}

case class HaarCascade(classifier: CascadeClassifier)

object HaarCascade {
  def apply(model: HaarCascadeModel): HaarCascade = {
    val haarModelFile = File.createTempFile(s"haar-model", ".xml")
    haarModelFile.deleteOnExit()
    // write the model file out to disk
    new BufferedWriter(new FileWriter(haarModelFile)) {
      write(
        Source
          .fromInputStream(getClass.getResourceAsStream(s"/${model.resource}"))
          .mkString)
      close()
    }
    HaarCascade(
      new CascadeClassifier() {
        load(haarModelFile.getPath)
      }
    )
  }

}
