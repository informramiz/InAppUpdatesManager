package github.informramiz.inappupdatesmanager.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by Ramiz Raja on 2019-08-04.
 */
inline fun <reified T> Gson.fromJson(string: String): T {
    //Java does not allow generic types retrieval at runtime
    //and because Gson using reflection so we need type information at runtime
    //so we make a subclass of TypeToken using which we can retrieve type info at runtime
    val type = object : TypeToken<T>() {}.type
    return Gson().fromJson<T>(string, type)
}