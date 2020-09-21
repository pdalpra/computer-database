module Pages.Top exposing (Model, Msg, Params, page)

import Spa.Document exposing (Document)
import Spa.Generated.Route as Routes
import Spa.Page as Page exposing (Page)
import Spa.Url exposing (Url)
import Utils.Routing as Routing


page : Page Params Model Msg
page =
    Page.element
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }



-- INIT


type alias Params =
    ()


type alias Model =
    {}


init : Url Params -> ( Model, Cmd Msg )
init { key } =
    ( {}, Routing.navigate key Routes.Computers__Top )



-- UPDATE


type Msg
    = Noop


update : Msg -> Model -> ( Model, Cmd Msg )
update _ model =
    ( model, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Document Msg
view _ =
    { title = "Top", body = [] }
