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
        Verbose,
        Debug,
        Info,
        Warn,
        Error,
        Assert,
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
        v{Lev.Verbose.name}
        tree.assert(index++, Log.VERBOSE,null, Lev.Verbose.name,null)
        d{Lev.Debug.name}
        tree.assert(index++, Log.DEBUG,null, Lev.Debug.name,null)
        i{Lev.Info.name}
        tree.assert(index++, Log.INFO,null, Lev.Info.name,null)
        w{Lev.Warn.name}
        tree.assert(index++,Log.WARN,null,Lev.Warn.name,null)
        e{Lev.Error.name}
        tree.assert(index,Log.ERROR,null,Lev.Error.name,null)
    }

    @Test @Throws(Exception::class) fun can_change_levels() {

        var index = 0
        tree.clear()
        startLogging(Log.WARN)
        assertEquals(0, tree.log.size)
        v{Lev.Verbose.name}
        assertEquals(0, tree.log.size)
        d{Lev.Debug.name}
        assertEquals(0, tree.log.size)
        i{Lev.Info.name}
        assertEquals(0, tree.log.size)
        w{Lev.Warn.name}
        tree.assert(index++,Log.WARN,null,Lev.Warn.name,null)
        e{Lev.Error.name}
        tree.assert(index,Log.ERROR,null,Lev.Error.name,null)
    }
}