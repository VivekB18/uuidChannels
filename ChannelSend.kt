import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import java.util.*
import kotlin.system.measureTimeMillis

class ChannelSend {
    val client = KMongo.createClient()
    val database = client.getDatabase("testBase")
    val collection = database.getCollection<uuidBatch>("ThreadTestWithNumberedChannels")

//    fun main() = runBlocking {
//        val time = measureTimeMillis {
//            val idProducer = produceBatches()
//            val mongoChannel = Channel<uuidBatch>()
//            batchMaker(idProducer = idProducer, mongoChannel = mongoChannel)
//            batchMaker(idProducer = idProducer, mongoChannel = mongoChannel)
//            batchMaker(idProducer = idProducer, mongoChannel = mongoChannel)
//            batchMaker(idProducer = idProducer, mongoChannel = mongoChannel)
//            batchMaker(idProducer = idProducer, mongoChannel = mongoChannel)
//            sendToMongo(mongoChannel)
//            sendToMongo(mongoChannel)
//            sendToMongo(mongoChannel)
//        }
//        println("time elapsed: $time")
//    }


    fun main() = runBlocking {
        val time = measureTimeMillis {
            val idProducer = produceBatches()
            val batchProducer1 = batchMaker(idProducer = idProducer)
            val batchProducer2 = batchMaker(idProducer = idProducer)
            val batchProducer3 = batchMaker(idProducer = idProducer)
            val batchProducer4 = batchMaker(idProducer = idProducer)
            val batchProducer5 = batchMaker(idProducer = idProducer)
            sendToMongo(batchProducer1)
            sendToMongo(batchProducer2)
            sendToMongo(batchProducer3)
            sendToMongo(batchProducer4)
            sendToMongo(batchProducer5)
        }
        println("time elapsed: $time")
    }



    fun CoroutineScope.produceBatches(): ReceiveChannel<UUID> = produce {
        for (i in 1..100000) {
            println(i)
            send(UUID.randomUUID())
        }
        //close()
    }

    fun CoroutineScope.batchMaker(idProducer: ReceiveChannel<UUID>):ReceiveChannel<uuidBatch> = produce {
        val block = mutableListOf<UUID>()
        for (id in idProducer)
        {
            block.add(id)
            if (block.size >= 500) {
                //println(block)
                send(uuidBatch(used = false, service = "", block = block))
              //  println("sent to Mongo function")
            }
        }
        //close()

    }

    suspend fun sendToMongo(mongoChannel: ReceiveChannel<uuidBatch>) {
        for (batch in mongoChannel) {
            //println("reached inside send")
            //println(batch)
            collection.insertOne(batch)
        }

    }
}