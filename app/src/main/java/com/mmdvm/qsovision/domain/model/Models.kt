package com.mmdvm.qsovision.domain.model
data class Talkgroup(val id:Int,val name:String="Unknown talkgroup")
data class CountryInfo(val code:String,val name:String)
data class QsoEvent(val id:String,val callsign:String,val talkgroup:Int,val startedAt:Long,val endedAt:Long?=null,val country:CountryInfo?=null)
enum class ConnectionState { CONNECTING, CONNECTED, IDLE, RECONNECTING, ERROR, OFFLINE }
data class TalkgroupSession(val slot:Int,val talkgroup:Talkgroup,val active:QsoEvent?=null,val muted:Boolean=false,val connection:ConnectionState=ConnectionState.CONNECTING)
