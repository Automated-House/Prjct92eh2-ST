/*
 *  Copyright 2019 prjct92eh2
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */


metadata {
		definition (name: "Virtual Motion With Switch", namespace: "prjct92eh2", author: "Jimmy Hawkins", mnmn: "SmartThings", vid: "generic-motion") {
		
	    capability "Sensor"
	    capability "Actuator"
	    capability "Switch"					//on, off
        capability "Motion Sensor"			//"active", "inactive"
        
        command "localOff"
        command "localOn"
        
        attribute "version", "string"
	}
    
    preferences {
    	def s1
        def s2
        def d
        //paragraph input
        input(
        	name			: "inSwitchOn"
            ,title			: "Switch (on, off)"
           	,type			: "bool"
            ,defaultValue	: true
        )
        input(
        	name			: "autoOff"
            ,title			: "Delayed device turn off (optional)"
            ,type			: "enum"
            ,required		: false
            ,options		: [["5":"5 seconds"],["30":"30 seconds"],["60":"1 Minute"],["300":"5 Minutes"]]
        )
        input( 
           	title			: "Device outputs\nSend the events listed below."
            ,description	: null
           	,type			: "icon"
            ,required		: false
            ,defaultValue	: "st.illuminance.illuminance.dark"
        )
        
		d = "Motion"
        s1 = "active"
        s2 = "inactive"
		input( 
        	name			: "motionOn"
            ,title			: buildTitle(d,s1,s2)
            ,type			: "enum"
            ,options		: buildOptions(d,s1,s2)
            ,description	: motionOn ?: "Not Used"            
        )        
    }
  
  	simulator {
	}

	// tile definitions
	tiles (scale:1) {
    	multiAttributeTile(name:"switch", type: "generic", width: 6, height: 2, canChangeIcon: true){
			tileAttribute ("device.uDTH", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "localOff", icon: "st.switches.switch.on", backgroundColor: "#53a7c0"
				attributeState "off", label: '${name}', action: "localOn", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
        }
        standardTile("motion", "device.motion", inactiveLabel: false, height:2, width:2, canChangeIcon: false) {
            state "default", label: "motion\nnot used" //, icon:"st.motion.motion.inactive"
            state "inactive", label:'${name}', backgroundColor: "#ffffff", icon:"st.motion.motion.inactive" 
            state "active", label:'${name}', backgroundColor: "#53a7c0", icon:"st.motion.motion.active" 
        }
        
        main(["switch"])
        details(["switch","motion"])
 	}
}

def on(){
	if (inSwitchOn) localOn()
}

def off(){
	if (inSwitchOn) localOff()
}

def buildTitle(d,s1,s2){
	return "${d} (${s1}, ${s2})"
}

def buildOptions(d,s1,s2){
	def sOn = "on"
    def sOff = "off"
	def options = []
    options.add(["1":"when ${sOn} set ${d} to '${s1}'\nwhen ${sOff} set ${d} to '${s2}'"])
    options.add(["0":"when ${sOn} set ${d} to '${s2}'\nwhen ${sOff} set ${d} to '${s1}'"])
    options.add(["-1":"Not Used"])
	return options
}

def syncDevices(cmd){
	if (cmd == null) cmd = device.currentValue("uDTH") == "on" ? "1" : "0"
    //log.debug "cmd: ${cmd}"
	if (motionOn in ["0","1"]){
		if (motionOn == cmd) sendEvent(name: "motion", value: "active")			//"when on send 'active'\nwhen off send 'inactive'"
        else sendEvent(name: "motion", value: "inactive")						//"when on send 'inactive'\nwhen off send 'active'"
    } else sendEvent(name: "motion", value: null, displayed	: false)
}

def localOn() {
	if (device.currentValue("uDTH") != "on"){
    	log.info "on request: OK"
		sendEvent(name: "uDTH", value: "on" ,displayed: false)
    	syncDevices("1")
        if (autoOff) runIn(autoOff.toInteger(),localOff)
    } else {
    	log.info "on request: duplicate, ignored"
    }
}

def localOff() {
	if (device.currentValue("uDTH") != "off"){
    	log.info "off request: OK"
		sendEvent(name: "uDTH", value: "off" ,displayed: false)
    	syncDevices("0")
    } else {
    	log.info "off request: duplicate, ignored"
    }
}

//capture preference changes
def updated() {
    //log.debug "syncDevices"
    syncDevices(null)
}

def configure() {

}