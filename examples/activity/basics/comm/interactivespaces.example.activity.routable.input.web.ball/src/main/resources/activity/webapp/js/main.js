var ws;
var canvas;
var ctx;

var circle;

var localOriginX;
var localOriginY;

function Circle(radius, centerX, centerY, color) {
	this.radius = radius;
	this.centerX = centerX;
	this.centerY = centerY;
	this.color = color;
}

Circle.prototype = {

	draw : function(ctx) {
		ctx.beginPath();
		ctx.arc(this.centerX, this.centerY, this.radius, 0, 2 * Math.PI, false);
		ctx.fillStyle = this.color;
		ctx.fill();
		ctx.lineWidth = 0;
		ctx.strokeStyle = this.color;
		ctx.stroke();
	},

	update : function(x, y) {
		this.centerX = x;
		this.centerY = y;
	}
};

$(document).ready(
		function() {
			ws = new WebSocket("ws://localhost:9001/websocket");

			ws.onopen = function(event) {
				$('#status').text("The WebSocket Connection Is Open.");
			}

			ws.onmessage = function(event) {
				var data = JSON.parse(event.data);
				console.log(data);

				var x = data.x;
				var y = data.y;
				var z = data.z;
				
				var localX = (x/360.0 + 1) * localOriginX;
				var localY = (y/360.0 + 1) * localOriginY;
				
				ctx.clearRect(0, 0, canvas.width, canvas.height);

				circle.update(localX, localY);
				circle.draw(ctx);
				

			}

			ws.onclose = function(event) {
				$('#status').text("The WebSocket Connection Has Been Closed.");
			}

			canvas = document.getElementById('myCanvas');
			ctx = canvas.getContext('2d');
			
			localOriginX = canvas.width/2;
			localOriginY = canvas.height/2;

			circle = new Circle(15, localOriginX, localOriginY,
					'rgba(61,148, 0, 0.95)');
			circle.draw(ctx);
		});
