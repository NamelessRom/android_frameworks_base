/*
 * <!--
 *    Copyright (C) 2014 The NamelessROM Project
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package com.android.internal.util.nameless;

public class ActionConstants {

    public static enum NamelessAction {
        ACTION_ONTHEGO_TOGGLE { @Override public String value() { return "**on_the_go**"; } },
        ACTION_SCREEN_RECORD { @Override public String value() { return "**screen_record**"; } },
        ACTION_TOGGLE_GESTURE_PANEL { @Override public String value() { return "**toggle_gesture_panel**"; } };

        public String value() { return "**null**"; }
    }

}
