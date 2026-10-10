package com.mmdvm.qsovision.country
import com.mmdvm.qsovision.domain.model.CountryInfo
class CountryResolver { private val cache=mutableMapOf<String,CountryInfo?>(); fun resolve(raw:String):CountryInfo? { val c=raw.uppercase().substringBefore('/'); return cache.getOrPut(c) { when { c.startsWith("9M") -> CountryInfo("🇲🇾","Malaysia"); c.startsWith("W")||c.startsWith("K")||c.startsWith("N") -> CountryInfo("🇺🇸","United States"); c.startsWith("G")||c.startsWith("M") -> CountryInfo("🇬🇧","United Kingdom"); else -> null } } } }
