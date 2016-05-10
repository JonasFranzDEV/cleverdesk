/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.cleverdesk.cleverdesk.launcher

import net.cleverdesk.cleverdesk.listener.Listener
import net.cleverdesk.cleverdesk.listener.ListenerManager
import net.cleverdesk.cleverdesk.plugin.Plugin
import net.cleverdesk.cleverdesk.plugin.PluginLoader
import net.cleverdesk.cleverdesk.web.WebServer
import spark.Spark
import java.io.File
import java.util.*

class Launcher {

    public val plugins: MutableList<Plugin> = LinkedList<Plugin>()


    public val listenerManager: ListenerManager = object : LinkedList<Listener>(), ListenerManager {}

    /**
     * The folder, where all data (like plugins) are.
     */
    public val dataFolder: File
        get() {
            return File("${Launcher::class.java.protectionDomain.codeSource.location.toURI().path.replace(".jar", "")}/")
        }

    public fun start() {
        println("License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>")
        println("This is free software: you are free to change and redistribute it.")
        println("There is NO WARRANTY, to the extent permitted by law.")

        //On ^C execute shutdown()
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { shutdown() }))

        //Create Folders (plugins etc.)
        if (!dataFolder.exists()) dataFolder.mkdirs()
        if (!File(dataFolder, "plugins/").exists()) File(dataFolder, "plugins/").mkdir()


        //Be sure that theire is no duplicate
        for (plugin in plugins) {
            plugin.disable()
        }
        plugins.clear()
        //Loading Plugins from plugins/*.jar
        plugins.addAll(PluginLoader().loadPlugins(this))
        //Enabling all plugins
        for (plugin in plugins) {
            plugin.enable()
        }
        var port = System.getenv("PORT")
        if (port == null) {
            port = System.getenv("port")
            if (port == null) {
                port = "8080"
            }
        }

        println("Starting WebServer on Port ${port}")
        //Start Web-Server
        WebServer.start(this, port.toInt())

    }

    public fun shutdown() {
        for (plugin in plugins) {
            plugin.disable()
        }
        println("Backend stopped")
        Spark.stop()

    }
}