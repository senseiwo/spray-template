package com.example

import akka.actor.Actor
import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.routing._
import spray.http._
import akka.event.slf4j._

case class TripRequestParams(startPoint:Option[String] = None,
                             coordinates:Option[String] = None,
                             activeness:Option[String] = None,
                             topics:Option[String] = None,
                             budget:Option[String] = None
                              )

case class Trip(name:String, price:Int, duration:Int)

object JsonImplicits extends DefaultJsonProtocol {
  implicit val impCluster = jsonFormat3(Trip)
}

class TripDao {
  def generate(prms:TripRequestParams): Either[String, List[Trip]] = {
    Right(List(Trip("trip1", 20, 4), Trip("trip2", 300, 5)))
    //Left("SOmethin wrong here")
  }
}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

trait MyService extends HttpService with SLF4JLogging {
  val dao = new TripDao

  val myRoute = respondWithMediaType(MediaTypes.`application/json`) {
    //import JsonImplicits._
    path("trips") {
      get {
        parameters(
          'startPointName.as[String] ?,
          'coordinates.as[String] ?,
          'activeness.as[String] ?,
          'topics.as[String] ?,
          'budget.as[String] ?)
          .as(TripRequestParams) {
          tripRqst:TripRequestParams=>{
            // we can call complete or withFailure also here, but uncomment this 'import JsonImplicits._'
            //complete{dao.generate(tripRqst)}

            ctx: RequestContext => {
              log.debug("REST: %s".format(tripRqst))
              requestHandler(ctx) {
                dao.generate(tripRqst)
              }
            }
          }
        }
      }
    }
  }
  def requestHandler(ctx:RequestContext)(action: =>Either[String,_]) = {
    import JsonImplicits._
    action match {
      case Right(response: List[Trip]) =>
        log.debug(">>>>>>>>  " + response.toString())
        ctx.complete{response}
      case Left(respone: Object) => ctx.failWith(new Throwable("Error Error Error " + respone))
      case _ => log.debug("not match")
    }
  }
}
