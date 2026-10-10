package com.mmdvm.qsovision
import com.mmdvm.qsovision.country.CountryResolver
import org.junit.Assert.*
import org.junit.Test
class CountryResolverTest { @Test fun knownPrefixResolves(){ assertEquals("Malaysia",CountryResolver().resolve("9M2ABC")?.name) }; @Test fun unknownIsNotFabricated(){ assertNull(CountryResolver().resolve("ZZ9ZZ")) } }
