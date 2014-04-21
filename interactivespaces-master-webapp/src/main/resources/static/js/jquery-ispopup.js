/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

(function( $ ){

  function fadeInPopup(event, id) {
    var tPosX = event.pageX + 20;
    var tPosY = event.pageY - 110;
	$(id).css({'position': 'absolute', 'top': tPosY, 'left': tPosX}).fadeIn();
  }

  function fadeOutPopup(event, id) {
    $(id).fadeOut();
  }

  $.fn.ispopup = function(popup_id) {  

    return this.each(function() {

      var $this = $(this);

      $this.mouseenter(function(event) {
        fadeInPopup(event, popup_id)
      })
      .mouseleave(function(event) {
        fadeOutPopup(event, popup_id)
      });
    });
  };
})( jQuery );
