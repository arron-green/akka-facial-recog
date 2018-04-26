package org.self

import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgproc._
import org.bytedeco.javacpp.opencv_objdetect.CASCADE_SCALE_IMAGE

import scala.concurrent.ExecutionContext

object Faces {

  val CV_AA = 16
  case class Face(id: Long,
                  face: Rect,
                  leftEye: Option[Rect] = None,
                  rightEye: Option[Rect] = None)

  case class Result(id: Long, rect: Rect)
  private def clone(rect: Rect): Rect =
    new Rect(rect.x, rect.y, rect.width, rect.height)

  def detect(img: Mat, model: HaarCascade): Seq[Result] = {
    val detected = new RectVector()
    model.classifier.detectMultiScale(img, detected)

    for { i <- 0L until detected.size() } yield Result(i, detected.get(i))
  }

  def eyes(img: Mat, f: Rect): Unit = {
    //TODO detect eyes on face
    val ler = new Rect(f.x, f.y, f.width / 2, f.height / 2)
    val lem = new Mat(img, ler)
    val le = new RectVector()
    leftEye.classifier.detectMultiScale(lem, le)
    val leRect = if (le.size() > 0) Some(clone(le.get(0))) else None

    val rer = new Rect(f.x + f.width / 2, f.y, f.width / 2, f.height / 2)
    val rem = new Mat(img, rer)
    val re = new RectVector()
    rightEye.classifier.detectMultiScale(rem, re)
    val reRect = if (re.size() > 0) Some(clone(re.get(0))) else None
  }

  lazy val eye = HaarCascade(Eye)
  lazy val eyeTreeEyeGlasses = HaarCascade(EyeTreeEyeGlasses)
  lazy val frontalCatFace = HaarCascade(FrontalCatFace)
  lazy val frontalCatFaceExt = HaarCascade(FrontalCatFaceExtended)
  lazy val frontFaceAlt = HaarCascade(FrontalFaceAlt)
  lazy val frontFaceAlt2 = HaarCascade(FrontalFaceAlt2)
  lazy val frontFaceAltTree = HaarCascade(FrontalFaceAltTree)
  lazy val frontFaceDefault = HaarCascade(FrontalFaceDefault)
  lazy val fullBody = HaarCascade(FullBody)
  lazy val hand = HaarCascade(Hand)
  lazy val leftEye = HaarCascade(LeftEye)
  lazy val leftEye2Splits = HaarCascade(LeftEye2Splits)
  lazy val lowerBody = HaarCascade(LowerBody)
  lazy val profileFace = HaarCascade(ProfileFace)
  lazy val rightEye = HaarCascade(RightEye)
  lazy val rightEye2Splits = HaarCascade(RightEye2Splits)
  lazy val smile = HaarCascade(Smile)
  lazy val upperBody = HaarCascade(UpperBody)

  def smileDetector(img: Mat)(implicit ec: ExecutionContext): Mat = {
    detectAndDraw(img, frontFaceAlt, smile, 1, false)(ec)
  }

  def detectAndDraw(img: Mat,
                    cascade: HaarCascade,
                    nestedCascade: HaarCascade,
                    scale: Double,
                    tryFlip: Boolean)(implicit ec: ExecutionContext): Mat = {

    val colors =
      Array(
        new Scalar(255, 0, 0, 0),
        new Scalar(255, 128, 0, 0),
        new Scalar(255, 255, 0, 0),
        new Scalar(0, 255, 0, 0),
        new Scalar(0, 128, 255, 0),
        new Scalar(0, 255, 255, 0),
        new Scalar(0, 0, 255, 0),
        new Scalar(255, 0, 255, 0)
      )

    val gray = new Mat
    val smallImg = new Mat

    cvtColor(img, gray, COLOR_BGR2GRAY)

    val fx: Double = 1.toDouble / scale

    resize(gray, smallImg, new Size(), fx, fx, INTER_LINEAR)
    equalizeHist(smallImg, smallImg)

    cascade.classifier.detectMultiScale(smallImg,
                                        new RectVector(),
                                        1.1,
                                        2,
                                        0 | CASCADE_SCALE_IMAGE,
                                        new Size(64, 64),
                                        new Size())

//    val faces2 = new RectVector()
//    if (tryFlip) {
//      flip(smallImg, smallImg, 1)
//      cascade.classifier.detectMultiScale(smallImg,
//                                          faces2,
//                                          scaleFactor,
//                                          minNeighbors,
//                                          flags,
//                                          minSize,
//                                          maxSize)
//
//      for { i <- 0L until faces2.size() } yield {
//        val r: Rect = faces2.get(i)
//        val rr =
//          new Rect(smallImg.cols - r.x - r.width, r.y, r.width, r.height)
//        faces.put(Array(new Rect()): _*)
//      }
//    }

    for { i <- 0L until new RectVector().size() } yield {
      val r = new RectVector().get(i)
      val color = colors((i % 8).toInt)
      var radius = 0

//      val aspectRatio = r.width.toDouble / r.height.toDouble
//
//      if (0.75 < aspectRatio && aspectRatio < 1.3) {
//        val center = new Point(cvRound((r.x + r.width * 0.5) * scale),
//                               cvRound((r.y + r.height * 0.5) * scale))
//        radius = cvRound((r.width + r.height) * 0.25 * scale)
//
//        val (thickness, lineWidth, shift) = (3, 8, 0)
//        circle(img, center, radius, color, thickness, lineWidth, shift)
//      } else {
//        val (thickness, lineWidth, shift) = (3, 8, 0)
//        rectangle(
//          img,
//          new Point(cvRound(r.x * scale), cvRound(r.y * scale)),
//          new Point(cvRound((r.x + r.width - 1) * scale),
//                    cvRound((r.y + r.height - 1) * scale)),
//          color,
//          thickness,
//          lineWidth,
//          shift
//        )
//      }

//      val halfHeight = cvRound(r.height.toFloat / 2)
//
//      // MEH!
//      r.y(r.y + halfHeight)
//      r.height(r.height - 1)

      val smallImgROI = smallImg(r)
      val nestedObjects = new RectVector()

      nestedCascade.classifier.detectMultiScale(smallImgROI,
                                                nestedObjects,
                                                1.1,
                                                0,
                                                0 | CASCADE_SCALE_IMAGE,
                                                new Size(64, 64),
                                                new Size())
    }
    img
  }

  def detectAndMark(img: Mat, greyImg: Mat): Mat = {
    for {
      f <- detect(greyImg, frontFaceAlt)
    } yield {
      detect(new Mat(greyImg, f.rect), smile) match {
        case smiles: Seq[Faces.Result] if smiles.nonEmpty =>
          println(s"Smile count detected: ${smiles.length}")
          rectangle(img, f.rect, new Scalar(0, 0, 255, 0), 3, 1, 0)
        case _ =>
      }
    }

    img
  }
}
