## Base Peripherals and Functions
* Panel
  * `getNetwork()` - returns the network Id of the panel
  * `getModules()` - returns a table of all the modules in the network
  * `getModulesOfType(string)` - returns a table of all modules of a certain type in the network, will return empty if none of the type are in the network or type does not exist
  * `getModule(string)` - returns a module if it's in the network, throws an exception if it it's not in the network
  * `getAllModuleTypes()` - returns a string table all registered module types
* Base module
  * `getMethods()` - returns a string table of all available methods for this module
  * `getType()` - returns a string of the module type
  * `getName()` - returns a string of the name of the module
  * `getX()`, `getY()` - returns a number of the panel position of the module (Note: currently only the position of the module it's on, not a global position)
  * `getSizeX()`, `getSizeY()` - returns a number of the size of the module e.g. the size of the switch is 2x3
---
## Module specific functions
* Switch
  * `getState()` - returns a boolean if the switch is on or off
* Control Lever
  * `getValue()` - returns a value between 0-15 of the lever signal
* Knob
  * `getAngle()` - returns the angle of the knob between 0-360
  * `getValue()` - returns a value between 0-15 of the mapped angle of the knob
* Indicator Bulb
  * `getState()` - returns a boolean of the current state of the bulb, on or off
  * `getColor()` - returns a serialized string of the current bulb color
  * `setState(bool)` - sets the state of the bulb, on or off
  * `setColor(string)` - sets the color of the bulb
  * List of all dyes that the bulb can be set to
    * `white`
    * `orange`
    * `magenta`
    * `light_blue`
    * `yellow`
    * `lime`
    * `pink`
    * `gray`
    * `light_gray`
    * `cyan`
    * `purple`
    * `blue`
    * `brown`
    * `green`
    * `red`
    * `black`
    * Dye depot dyes
      * `maroon`
      * `rose`
      * `coral`
      * `indigo`
      * `navy`
      * `slate`
      * `olive`
      * `teal`
      * `mint`
      * `aqua`
      * `verdant`
      * `forest`
      * `ginger`
      * `tan`
---
## Example code
```lua
local panel = peripheral.wrap("back")
local monitor = peripheral.wrap("top")

while true do
  monitor.clear()

  offset = 1
  for i, v in ipairs(panel.getModulesOfType("switch")) do
    monitor.setCursorPos(1, offset)
    monitor.write(string.format("%s : %s", v.getName(), tostring(v.getState())))
    offset = offset + 1
  end
  for i, v in ipairs(panel.getModulesOfType("knob")) do
    monitor.setCursorPos(1, offset)
    monitor.write(string.format("%s : %s", v.getName(), tostring(v.getAngle())))
    offset = offset + 1
  end
  for i, v in ipairs(panel.getModulesOfType("control_lever")) do
    monitor.setCursorPos(1, offset)
    monitor.write(string.format("%s : %s", v.getName(), tostring(v.getValue())))
    offset = offset + 1
  end
  for i, v in ipairs(panel.getModulesOfType("indicator_bulb")) do
    monitor.setCursorPos(1, offset)
    monitor.write(string.format("%s : %s", v.getName(), tostring(v.getState())))
    offset = offset + 1
  end
  os.sleep(0.01)
end
```