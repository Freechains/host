package org.freechains.host

import org.freechains.common.*
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.system.exitProcess

val help = """
freechains-host $VERSION

Usage:
    freechains-host start <dir>
    freechains-host stop
    freechains-host now <time>
    
Options:
    --host=<addr:port>      port to connect [default: $PORT_8330]

More Information:

    http://www.freechains.org/

    Please report bugs at <http://github.com/Freechains/core>.
"""

fun main (args: Array<String>) {
    main_host(args).let { (ok,msg) ->
        if (ok) {
            if (msg.isNotEmpty()) {
                println(msg)
            }
        } else {
            System.err.println(msg)
            exitProcess(1)
        }
    }
}

fun main_host (args: Array<String>) : Pair<Boolean,String> {
    return catch_all("freechains-host ${args.joinToString(" ")}") {
        val (cmds, opts) = args.cmds_opts()
        //println(">>> $cmds")
        //println(">>> $opts // ${opts.containsKey("--port")}")
        val port = if (opts.containsKey("--port")) opts["--port"]!!.toInt() else PORT_8330

        when {
            opts.containsKey("--help") -> Pair(true, help)
            (cmds.size == 0) -> Pair(false, help)
            else -> when (cmds[0]) {
                "start" -> {
                    assert_(cmds.size == 2)
                    val host = Host_load(cmds[1], port)
                    println("Freechains $VERSION")
                    println("Waiting for connections on $host...")
                    Daemon(host).daemon()
                    Pair(true, "")
                }
                else -> {
                    val socket = Socket_5s("localhost", port)
                    val writer = DataOutputStream(socket.getOutputStream()!!)
                    val reader = DataInputStream(socket.getInputStream()!!)
                    when (cmds[0]) {
                        "stop" -> {
                            assert_(cmds.size == 1)
                            writer.writeLineX("$PRE host stop")
                            assert_(reader.readLineX() == "true")
                            socket.close()
                            Pair(true, "")
                        }
                        "now" -> {
                            assert_(cmds.size == 2)
                            writer.writeLineX("$PRE host now ${cmds[1]}")
                            assert_(reader.readLineX() == "true")
                            socket.close()
                            Pair(true, "")
                        }
                        else -> Pair(false, help)
                    }
                }
            }
        }
    }
}