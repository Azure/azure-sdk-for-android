package com.azure.core

import android.util.Log
import com.azure.core.log.*
import org.junit.After
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before
import timber.log.Timber

class LogTest {

    enum class Lev {
        ZERO_IS_NOT_A_LEVEL,
        ONE_IS_NOT_A_LEVEL,
        verbose,
        debug,
        info,
        warn,
        error,
        assert,
    }

    class ArrayTree : Timber.Tree() {
        data class Entry( val priority: Int, val tag: String?, val message: String, val throwable: Throwable?)
        val log = ArrayList<Entry>()
        override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            log.add(Entry(priority,tag,message,throwable))
        }
        fun clear() : ArrayTree {
            log.clear()
            return this
        }
        fun assert(index: Int, priority: Int, tag: String?, message: String, throwable: Throwable?) {
            assertEquals(true,index<log.size)
            assertEquals(priority, log[index].priority)
            assertEquals(tag, log[index].tag)
            assertEquals(message, log[index].message)
            assertEquals(throwable, log[index].throwable)
        }
    }

    private val tree = ArrayTree()

    @Before
    fun before() {
        startLogging(tree)
    }

    @After
    fun after() {
        stopLogging(tree)
    }

    @Test @Throws(Exception::class) fun can_log_each_level() {
        var index = 0
        tree.clear()
        assertEquals(0, tree.log.size)
        v{Lev.verbose.name}
        tree.assert(index++, Log.VERBOSE,null, Lev.verbose.name,null)
        d{Lev.debug.name}
        tree.assert(index++, Log.DEBUG,null, Lev.debug.name,null)
        i{Lev.info.name}
        tree.assert(index++, Log.INFO,null, Lev.info.name,null)
        w{Lev.warn.name}
        tree.assert(index++,Log.WARN,null,Lev.warn.name,null)
        e{Lev.error.name}
        tree.assert(index,Log.ERROR,null,Lev.error.name,null)
    }

    @Test @Throws(Exception::class) fun can_change_levels() {
        var index = 0
        tree.clear()
        startLogging(Log.WARN)
        assertEquals(0, tree.log.size)
        v{Lev.verbose.name}
        assertEquals(0, tree.log.size)
        d{Lev.debug.name}
        assertEquals(0, tree.log.size)
        i{Lev.info.name}
        assertEquals(0, tree.log.size)
        w{Lev.warn.name}
        tree.assert(index++,Log.WARN,null,Lev.warn.name,null)
        e{Lev.error.name}
        tree.assert(index++,Log.ERROR,null,Lev.error.name,null)
    }
}