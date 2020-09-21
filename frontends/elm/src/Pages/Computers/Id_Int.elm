module Pages.Computers.Id_Int exposing (Model, Msg, Params, page)

import Api exposing (UnsavedComputer)
import Browser.Navigation as Nav
import Http
import Model.Company as Company
import Model.Computer as Computer exposing (Computer)
import Shared
import Spa.Document exposing (Document)
import Spa.Generated.Route as Routes
import Spa.Page as Page exposing (Page)
import Spa.Url exposing (Url)
import Time exposing (Posix)
import Utils.Routing as Routing


page : Page Params Model Msg
page =
    Page.application
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        , save = save
        , load = load
        }



-- INIT


type alias Params =
    { id : Int }


type alias Model =
    { key : Nav.Key, settings : Api.Settings, computer : Maybe UnsavedComputer }


init : Shared.Model -> Url Params -> ( Model, Cmd Msg )
init shared { params } =
    let
        model =
            { key = shared.key, settings = shared.apiSettings, computer = Nothing }
    in
    case Computer.idFromInt params.id of
        Just id ->
            ( model, Api.fetchComputer id GotComputer model.settings )

        Nothing ->
            ( model, Routing.navigate shared.key Routes.NotFound )



-- UPDATE


type Msg
    = Name String
    | Introduced (Maybe Posix)
    | Discontinued (Maybe Posix)
    | Company (Maybe Company.Id)
    | GotComputer (Result Http.Error Computer)
    | Save
    | Cancel


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case model.computer of
        Nothing ->
            case msg of
                GotComputer (Ok computer) ->
                    ( { model | computer = Just (Api.toUnsavedComputer computer) }, Cmd.none )

                GotComputer (Err (Http.BadStatus 404)) ->
                    ( model, Routing.navigate model.key Routes.NotFound )

                _ ->
                    ( model, Cmd.none )

        Just computer ->
            case msg of
                Name name ->
                    ( { model | computer = Just { computer | name = name } }, Cmd.none )

                Introduced introduced ->
                    ( { model | computer = Just { computer | introduced = introduced } }, Cmd.none )

                Discontinued discontinued ->
                    ( { model | computer = Just { computer | discontinued = discontinued } }, Cmd.none )

                Company company ->
                    ( { model | computer = Just { computer | company = company } }, Cmd.none )

                GotComputer (Ok updated) ->
                    ( { model | computer = Just (Api.toUnsavedComputer updated) }, Cmd.none )

                GotComputer (Err error) ->
                    ( model, Cmd.none )

                Save ->
                    -- TODO
                    ( model, Cmd.none )

                Cancel ->
                    ( model, Routing.navigate model.key Routes.Computers__Top )


save : Model -> Shared.Model -> Shared.Model
save _ shared =
    shared


load : Shared.Model -> Model -> ( Model, Cmd Msg )
load _ model =
    ( model, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Model -> Document Msg
view model =
    { title = "Computer.Id_Int"
    , body = []
    }
