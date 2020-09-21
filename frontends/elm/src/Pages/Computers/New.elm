module Pages.Computers.New exposing (Model, Msg, Params, page)

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
    ()


type alias Model =
    { key : Nav.Key, settings : Api.Settings, computer : UnsavedComputer }


init : Shared.Model -> Url Params -> ( Model, Cmd Msg )
init shared _ =
    let
        computer =
            { id = Nothing, name = "", introduced = Nothing, discontinued = Nothing, company = Nothing }
    in
    ( { key = shared.key, settings = shared.apiSettings, computer = computer }, Cmd.none )



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
    let
        computer =
            model.computer
    in
    case msg of
        Name name ->
            ( { model | computer = { computer | name = name } }, Cmd.none )

        Introduced introduced ->
            ( { model | computer = { computer | introduced = introduced } }, Cmd.none )

        Discontinued discontinued ->
            ( { model | computer = { computer | discontinued = discontinued } }, Cmd.none )

        Company company ->
            ( { model | computer = { computer | company = company } }, Cmd.none )

        GotComputer (Ok created) ->
            ( model, Routing.navigate model.key (Routes.Computers__Id_Int { id = Computer.idToInt created.id }) )

        GotComputer (Err err) ->
            -- TODO
            ( model, Cmd.none )

        Save ->
            ( model, Api.createComputer model.computer GotComputer model.settings )

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
    { title = "Computer.New"
    , body = []
    }
