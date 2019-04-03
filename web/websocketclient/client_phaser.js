var socket;
var socketID = "";

var config = {
    type: Phaser.AUTO,
    width: 880,
    height: 880,
    backgroundColor: '#b8b8b8',
    parent: 'map',

    scene: {
        preload: preload,
        create: create,

    }
    //scene: [Credits, Instructions, MainMenu, Pause, EndGame, JuegoScene]
};

var game = new Phaser.Game(config);

function preload() {
    //game.load.tilemap('map', 'assets/map/example_map.json', null, Phaser.Tilemap.TILED_JSON);
    //game.load.spritesheet('tileset', 'assets/map/tilesheet.png',32,32);
    //game.load.image('sprite','assets/sprites/sprite.png');
    this.load.image('mapa', 'images/mapa_solo.jpeg');
}

function create() {
    //background = this.add.image(440, 440, 'mapa');
}

function fire(x, y) {
    socket.send('{"name": "fire", "priority": "1","parameters": [{"name": "x", "value": "' + x + '"},{"name": "y", "value": "' + y + '"}]}');
}

window.onload = function () {
    var page = document.createElement('a');
    page.href = window.location.href;
    //define la url del servidor como la hostname de la pagina y el puerto definido 8080 del ws
    var url = "ws://" + page.hostname + ":8080";
    socket = new WebSocket(url + "/StateEngine/GameWebSocket");
    socket.onmessage = stateUpdate;

    var map = document.getElementById("map");
    var scene = document.getElementById("scene");
    var terrain = document.getElementById("terrain");
    var entities = document.getElementById("entities");
    var ready = document.getElementById("ready");
    var restart = document.getElementById("restart");
    var playerTeam = [];
    //var sendAction = document.getElementById("sendAction");
    //var action = document.getElementById("action");

    //actualiza la vista del juego cuando recive un nuevo estado desde el servidor
    function stateUpdate(event) {
        //console.log(socket);
        //console.log(event.data);
        var gameState = JSON.parse(event.data);
        console.log(gameState);
        //var game2State = event.data;
        if (typeof gameState !== "undefined") {
            //console.log(game2State);
            if (gameState["id"] !== "undefined" && socketID === "") {
                socketID = gameState["id"];
                //console.log(socketID);
            }
            var i = 0;
            while (typeof gameState[i] !== "undefined") {
                //console.log(game2State[i]);
                if (typeof gameState[i]["Remove"] !== "undefined") {
                    var id = gameState[i]["Remove"]["id"];
                    $("#" + id).remove();
                } else if (typeof gameState[i]["Map"] !== "undefined") {
                    //console.log(gameState[i]["Map"]);
                    var width = gameState[i]["Map"]["width"];
                    var height = gameState[i]["Map"]["height"];
                    var j = 0;
                    var x;
                    var y;
                    var val;
                    var cell;
                    while (typeof gameState[i]["Map"]["cells"][j] !== "undefined") {
                        x = gameState[i]["Map"]["cells"][j]["x"];
                        y = gameState[i]["Map"]["cells"][j]["y"];
                        val = gameState[i]["Map"]["cells"][j]["val"];
                        if (gameState[i]["Map"]["cells"][j]["val"] == 1) {
                            terrain.innerHTML += "<div id='cell" + x + "_" + y + "' class='cell wall" + val + "'></div>";
                            cell = document.getElementById("cell" + x + "_" + y);
                            cell.style.left = x * ($(".cell").width() + 2) + "px";
                            cell.style.top = y * ($(".cell").height() + 2) + "px";
                            cell.style.visibility = "visible";
                        }
                        j++;
                    }
                    terrain.style.width = width * ($(".cell").width + 2) + "px";
                    terrain.style.height = height * ($(".cell").height() + 2) + "px";
                    //game2.style.width = width * ($(".cell").width() + 2) + "px";

                    scene.style.width = width * ($(".cell").width() + 2) + "px";
                    scene.style.height = height * ($(".cell").height() + 2) + "px";

                    //terrain.style.height = height * ($(".cell").height() + 2) + "px";
                    //game2.style.height = height * ($(".cell").height() + 2) + "px";
                } else if (typeof gameState[i]["Player"] !== "undefined") {
                    //console.log(gameState[i]["Player"]);
                    var id = gameState[i]["Player"]["super"]["Entity"]["super"]["State"]["id"];
                    var playerId = gameState[i]["Player"]["id"];
                    var destroy = gameState[i]["Player"]["super"]["Entity"]["super"]["State"]["destroy"];
                    var leave = gameState[i]["Player"]["leave"];
                    var dead = gameState[i]["Player"]["dead"];
                    var health = gameState[i]["Player"]["health"];
                    var healthMax = gameState[i]["Player"]["healthMax"];
                    var x = gameState[i]["Player"]["super"]["Entity"]["x"];
                    var y = gameState[i]["Player"]["super"]["Entity"]["y"];
                    var team = gameState[i]["Player"]["team"];
                    playerTeam[playerId] = team;
                    var player = document.getElementById(id);
                    if (player === null) {
                        var name = id.substr(0, 4);
                        entities.innerHTML +=
                                "<div id='" + id + "' class='player'>" +
                                "<div id='" + id + "-healthbar' class='healthbar'></div>" +
                                "<div id='" + id + "-name' class='name'>" + name + "</div>" +
                                "<div id='" + id + "-visibility' class='visibility'></div>" +
                                "</div>";
                        player = document.getElementById(id);
                    }
                    player.style.left = x * ($(".cell").width() + 2) + 1 + "px";
                    player.style.top = y * ($(".cell").height() + 2) + 1 + "px";
                    playerHealthbar = document.getElementById(id + "-healthbar");
                    playerHealthbar.style.width = health * 24 / healthMax + "px";
                    playerVisibility = document.getElementById(id + "-visibility");
                    playerVisibility.style.visibility = "hidden";
                    if (dead) {
                        player.style.zIndex = "1";
                        player.style.backgroundImage = "url('images/blood.png')";
                        player.style.backgroundColor = "rgba(0,0,0,0)";
                    } else {
                        player.style.zIndex = "2";
                        player.style.backgroundImage = "url('images/brujo.png')";
                        if (playerId === socketID) {
                            player.style.backgroundColor = "green";
                            playerVisibility.style.visibility = "visible";
                        } else {
                            player.style.backgroundColor = $(".team" + team).css("color");//"#" + ((1 << 24) * Math.random() | 0).toString(16);
                        }
                    }
                    if (leave) {
                        player.style.display = "none";
                    } else {
                        player.style.display = "block";
                    }
                    if (destroy) {
                        $("#" + id).remove();
                        playerTeam[id] = "";
                    }
                    //players.innerHTML += game2State[i]["Player"]["id"] + "," + game2State[i]["Player"]["x"] + "," + game2State[i]["Player"]["y"] + "<br>";
                } else if (typeof gameState[i]["Projectile"] !== "undefined") {
                    //console.log(game2State[i]["Projectile"]);
                    var id = gameState[i]["Projectile"]["super"]["Entity"]["super"]["State"]["id"];
                    var playerId = gameState[i]["Projectile"]["id"];
                    var number = gameState[i]["Projectile"]["number"];
                    var destroy = gameState[i]["Projectile"]["super"]["Entity"]["super"]["State"]["destroy"];
                    var x = gameState[i]["Projectile"]["super"]["Entity"]["x"];
                    var y = gameState[i]["Projectile"]["super"]["Entity"]["y"];
                    var xVelocity = gameState[i]["Projectile"]["xVelocity"];
                    var yVelocity = gameState[i]["Projectile"]["yVelocity"];
                    var team = gameState[i]["Projectile"]["team"];
                    var projectile = document.getElementById(id);
                    if (projectile === null) {
                        entities.innerHTML +=
                                "<div id='" + id + "' class='arrow'>" +
                                "<div id='projectileTeam" + id + "' class='arrowteam'></div>" +
                                "</div>";
                        projectile = document.getElementById(id);
                        var projectileTeam = document.getElementById("projectileTeam" + id);
                        if (playerId === socketID) {
                            projectileTeam.style.backgroundColor = "green";
                        } else {
                            projectileTeam.style.backgroundColor = $(".team" + team).css("color");
                        }

                    }
                    projectile.style.left = x * ($(".cell").width() + 2) + 1 + ($(".cell").width() + 2) / 2 - $(".arrow").width() / 2 + "px";
                    projectile.style.top = y * ($(".cell").height() + 2) + 1 + ($(".cell").height() + 2) / 2 - $(".arrow").height() / 2 + "px";
                    if (destroy) {
                        $("#" + id).remove();
                    }
                    //players.innerHTML += game2State[i]["Player"]["id"] + "," + game2State[i]["Player"]["x"] + "," + game2State[i]["Player"]["y"] + "<br>";
                } else if (typeof gameState[i]["Tower"] !== "undefined") {
                    //console.log(gameState[i]["Tower"]);
                    var id = gameState[i]["Tower"]["super"]["Entity"]["super"]["State"]["id"];
                    var playerId = gameState[i]["Tower"]["id"];
                    var destroy = gameState[i]["Tower"]["super"]["Entity"]["super"]["State"]["destroy"];
                    var dead = gameState[i]["Tower"]["dead"];
                    var team = gameState[i]["Tower"]["team"];
                    var health = gameState[i]["Tower"]["health"];
                    var healthMax = gameState[i]["Tower"]["healthMax"];
                    var width = gameState[i]["Tower"]["width"];
                    var height = gameState[i]["Tower"]["height"];
                    var x = gameState[i]["Tower"]["super"]["Entity"]["x"];
                    var y = gameState[i]["Tower"]["super"]["Entity"]["y"];
                    var team = gameState[i]["Tower"]["team"];
                    var tower = document.getElementById(id);
                    if (tower === null) {
                        entities.innerHTML +=
                                "<div id='" + id + "' class='tower'>" +
                                "<div id='" + id + "-healthbar' class='healthbar'></div>" +
                                "</div>";
                        tower = document.getElementById(id);
                        //tower.style.backgroundColor = $(".team" + team).css("color");
                    }
                    tower.style.left = x * ($(".cell").width() + 2) + 1 - (Math.floor(width / 2) * $(".cell").width()) + "px";
                    tower.style.top = -60 + y * ($(".cell").height() + 2) + 1 - (Math.floor(height / 2) * $(".cell").width()) + "px";
                    towerHealthbar = document.getElementById(id + "-healthbar");
                    towerHealthbar.style.width = health * width * 24 / healthMax + "px";
                    towerHealthbar.style.left = -8 + "px";
                    if (dead) {
                        //tower.style.zIndex = "1";
                        //tower.style.removeProperty('backgroundImage');
                        tower.style.backgroundImage = "url('images/mapa_2018/torre_rota.png')";
                        tower.style.backgroundSize = "100% 50%";
                        tower.style.marginTop = "45px";
                        //tower.style.backgroundSize ="100% 50%";
                        //tower.style.marginTop = "45px";
                    }
                    if (destroy) {
                        $("#" + id).remove();
                    }
                } else if (typeof gameState[i]["Spawn"] !== "undefined") {
                    var x = gameState[i]["Spawn"]["x"];
                    var y = gameState[i]["Spawn"]["y"];
                    terrain.innerHTML += "<div id='spawn" + x + "_" + y + "' class='cell spawn'></div>";
                    spawn = document.getElementById("spawn" + x + "_" + y);
                    spawn.style.left = x * ($(".cell").width() + 2) + "px";
                    spawn.style.top = y * ($(".cell").height() + 2) + "px";
                } else if (typeof gameState[i]["Match"] !== "undefined") {
                    //console.log(game2State[i]["Match"]);
                    var round = gameState[i]["Match"]["round"];
                    document.getElementById("round").innerHTML = round;
                    var countRounds = gameState[i]["Match"]["countRounds"];
                    document.getElementById("countRounds").innerHTML = countRounds;
                    var endGame = gameState[i]["Match"]["endGame"];
                    var endRound = gameState[i]["Match"]["endRound"];
                    var startGame = gameState[i]["Match"]["startGame"];
                    var teamAttacker = gameState[i]["Match"]["teamAttacker"];
                    var sizeTeam = gameState[i]["Match"]["sizeTeam"];
                    var players = gameState[i]["Match"]["players"];
                    var ready = gameState[i]["Match"]["ready"];
                    var playersDOM = document.getElementById("players");
                    playersDOM.innerHTML = "<tr><th>ID</th><th>Ready</th></tr>";
                    for (var p = 0; p < players.length; p++) {
                        var found = false;
                        var r = 0;
                        var playerReady = null;
                        while (!found && r < ready.length) {
                            if (ready[r] === players[p]) {
                                playerReady = ready[r];
                                found = true;
                            }
                            r++;
                        }
                        var checked = playerReady ? "checked" : "";
                        var team = playerTeam[players[p]];
                        if (team === "undefined") {
                            team = "";
                        }
                        playersDOM.innerHTML += "<tr><td class='team" + team + "'>" + players[p] + "</td><td><input id='ready" + player + "' type='checkbox' disabled " + checked + "/></td></tr>";
                    }
                    var teamPoints = gameState[i]["Match"]["teamPoints"];
                    var teamPointsDOM = document.getElementById("teamPoints");
                    teamPointsDOM.innerHTML = "";
                    for (var p = 0; p < teamPoints.length; p++) {
                        teamPointsDOM.innerHTML += "<span class='team" + p + "'>" + teamPoints[p] + "</span> ";
                    }
                }
                i++;
            }
        }
    }


    //evento al presionar el boton de Enviar Accion
    /*sendAction.addEventListener("click", function () {
     var actionValue = action.options[action.selectedIndex].value;
     socket.send(actionValue);
     });*/

    ready.addEventListener("click", function () {
        socket.send("ready");
    });

    restart.addEventListener("click", function () {
        socket.send("restart");
    });

    var Key = {
        _pressed: {},

        LEFT: 37,
        UP: 38,
        RIGHT: 39,
        DOWN: 40,
        FIRE: 32,
        ALTLEFT: 65,
        ALTUP: 87,
        ALTRIGHT: 68,
        ALTDOWN: 83,

        areDown: function (keyCodes) {
            var pressed = false;
            var i = 0;
            while (!pressed && i < keyCodes.length) {
                pressed = this._pressed[keyCodes[i]];
                i++;
            }
            return pressed;
        },

        isDown: function (keyCode) {
            return this._pressed[keyCode];
        },

        onKeydown: function (event) {
            this._pressed[event.keyCode] = true;
        },

        onKeyup: function (event) {
            delete this._pressed[event.keyCode];
        }
    };

    window.addEventListener('keyup', function (event) {
        Key.onKeyup(event);
    }, false);
    window.addEventListener('keydown', function (event) {
        Key.onKeydown(event);
    }, false);

    window.setInterval(function () {
        updateKeyboard();
    }, 100);

    function updateKeyboard() {
        /*if (Key.areDown([Key.UP, Key.ALTUP]) && Key.areDown([Key.LEFT, Key.ALTLEFT])) {
         socket.send("upleft");
         } else if (Key.areDown([Key.UP, Key.ALTUP]) && Key.areDown([Key.RIGHT, Key.ALTRIGHT])) {
         socket.send("upright");
         } else if (Key.areDown([Key.DOWN, Key.ALTDOWN]) && Key.areDown([Key.LEFT, Key.ALTLEFT])) {
         socket.send("downleft");
         } else if (Key.areDown([Key.DOWN, Key.ALTDOWN]) && Key.areDown([Key.RIGHT, Key.ALTRIGHT])) {
         socket.send("downright");
         } else */if (Key.areDown([Key.UP, Key.ALTUP])) {
            socket.send("up");
        } else if (Key.areDown([Key.LEFT, Key.ALTLEFT])) {
            socket.send("left");
        } else if (Key.areDown([Key.DOWN, Key.ALTDOWN])) {
            socket.send("down");
        } else if (Key.areDown([Key.RIGHT, Key.ALTRIGHT])) {
            socket.send("right");
        }
        if (Key.isDown(Key.FIRE)) {
            fire(1, 1);
        }
    }

    //evento para fire 
    $(document).ready(function () {
        $("#scene").mousedown(function (event) {
            var relX = event.pageX - $(this).offset().left;
            var relY = event.pageY - $(this).offset().top;
            relX = parseInt(relX / ($(".cell").width() + 2));
            relY = parseInt(relY / ($(".cell").height() + 2));
            //console.log("(" + relX + "," + relY + ")");
            fire(relX, relY);
        });
    });
}

/*Game.create = function(){
 Game.playerMap = {};
 var testKey = game2.input.keyboard.addKey(Phaser.Keyboard.ENTER);
 testKey.onDown.add(Client.sendTest, this);
 var map = game2.add.tilemap('map');
 map.addTilesetImage('tilesheet', 'tileset'); // tilesheet is the key of the tileset in map's JSON file
 var layer;
 for(var i = 0; i < map.layers.length; i++) {
 layer = map.createLayer(i);
 }
 layer.inputEnabled = true; // Allows clicking on the map ; it's enough to do it on the last layer
 layer.events.onInputUp.add(Game.getCoordinates, this);
 Client.askNewPlayer();
 };
 
 Game.getCoordinates = function(layer,pointer){
 Client.sendClick(pointer.worldX,pointer.worldY);
 };
 
 Game.addNewPlayer = function(id,x,y){
 Game.playerMap[id] = game2.add.sprite(x,y,'sprite');
 };
 
 Game.movePlayer = function(id,x,y){
 var player = Game.playerMap[id];
 var distance = Phaser.Math.distance(player.x,player.y,x,y);
 var tween = game2.add.tween(player);
 var duration = distance*10;
 tween.to({x:x,y:y}, duration);
 tween.start();
 };
 
 Game.removePlayer = function(id){
 Game.playerMap[id].destroy();
 delete Game.playerMap[id];
 };*/