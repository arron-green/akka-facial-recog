package org.self

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.{Done, NotUsed}
import org.bytedeco.javacpp.opencv_core.{CV_8U, Mat, flip}
import org.bytedeco.javacpp.opencv_imgproc._ //{CV_BGR2GRAY, cvtColor, equalizeHist}
import org.bytedeco.javacv._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object ImageFlipper {
  sealed trait ImageDirection { val value: Int }
  case object XAxis extends ImageDirection { val value = 0 }
  case object YAxis extends ImageDirection { val value = 1 }
  case object BothAxis extends ImageDirection { val value = -1 }
  def horizontal(mat: Mat, direction: ImageDirection = YAxis): Mat = {
    val matCloned = mat.clone()
    flip(mat, matCloned, direction.value)
    matCloned
  }
  def horizontal(mat: Mat): Mat = horizontal(mat, YAxis)
  def vertical(mat: Mat): Mat = horizontal(mat, XAxis)
  def both(mat: Mat): Mat = horizontal(mat, BothAxis)
}

object FrameConverter {
  lazy val converter = new OpenCVFrameConverter.ToMat()
  def toMat(frame: Frame): Mat = converter.convert(frame)
  def toFrame(mat: Mat): Frame = converter.convert(mat)
}

object GrayConverter {
  def toGray(mat: Mat): (Mat, Mat) = {
    if (mat.channels() == 1) return (mat, mat)
    val greyMat = new Mat(mat.rows(), mat.cols(), CV_8U)
    cvtColor(mat, greyMat, CV_BGR2GRAY, 1)
    equalizeHist(greyMat, greyMat)
    (mat, greyMat)
  }
}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("Faces")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  object VideoStream {
    private val elements = 1
    private val duration = 200 milliseconds
    private val maxBurst = 1

    private def deviceSource(sourceId: String): Source[Frame, NotUsed] = {
      val grabber = new FFmpegFrameGrabber(sourceId) {
        setFormat("avfoundation")
        setFrameRate(24)
        setImageWidth(640)
        setImageHeight(480)
        setBitsPerPixel(CV_8U)
        start()
      }

      Source
        .fromIterator(() => Iterator.continually(grabber.grab()))
        .throttle(elements, duration, maxBurst, ThrottleMode.Shaping)
    }

    def source(sourceId: String): Source[Mat, NotUsed] =
      deviceSource(sourceId).map(FrameConverter.toMat)
  }

  object Display {
    private lazy val canvas = new CanvasFrame("webcam") {
      setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE)
    }
    def show(frame: Frame): Unit = canvas.showImage(frame)
  }

  def sink(): Sink[Frame, Future[Done]] = Sink.foreach(Display.show)

  // hardcoded for demo purposes
//  val cameraSourceId = "/dev/video0"
  val cameraSourceId = "FaceTime HD Camera"

  VideoStream
    .source(cameraSourceId)
    .map(ImageFlipper.horizontal)
    .map(GrayConverter.toGray)
    .map { case (m, gm) => Faces.detectAndMark(m, gm) }
//    .map(f => Faces.smileDetector(f)(executor.prepare()))
    .map(FrameConverter.toFrame)
    .to(sink())
    .run()(materializer)

}
