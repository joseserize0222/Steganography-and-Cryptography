    package cryptography
    import java.awt.Color
    import java.awt.image.BufferedImage
    import java.io.File
    import javax.imageio.ImageIO
    import kotlin.experimental.and

    class ImageChanger(private val path: File){
        private val image: BufferedImage = ImageIO.read(path)

        fun encryptImage(message: String, password: String){
            if(message.length*8 + 24 >= (image.width * image.height)){
                throw Exception("The input image is not large enough to hold this message.")
            }
            var arrayByte: ByteArray = message.encodeToByteArray()
            val arrayPassword = password.encodeToByteArray()
            for(i in arrayByte.indices){
                arrayByte[i] = (arrayByte[i].toInt() xor arrayPassword[i % arrayPassword.size].toInt()).toByte()
            }
            arrayByte += byteArrayOf(0, 0, 3)
            var counter = 7
            var pos = 0
            loop@ for(i in 0 until image.height){
                for(j in 0 until image.width){
                    val color = Color(image.getRGB(j, i))
                    val newColor = Color(color.red, color.green, if((arrayByte[pos].toInt() and (1 shl counter)) > 0) (color.blue or 1) else color.blue and (color.blue xor 1) )
                    image.setRGB(j, i, newColor.rgb)
                    if(counter == 0){
                        counter = 8
                        pos++
                    }
                    counter--
                    if(pos >= arrayByte.size) break@loop
                }
            }
        }
        fun decryptImage(password: String): String{
            val list = mutableListOf<Byte>(0)
            var bit = 7
            loop@for(i in 0 until image.height){
                for(j in 0 until image.width){
                    val color = Color(image.getRGB(j, i))
                    val constructedBit: Int = color.blue
                    if(bit == 7){
                        list.add(0)
                        if(list.size >= 4){
                            if((list[list.size - 2] == 3.toByte()) and (list[list.size - 3] == 0.toByte()) and (list[list.size -4] == 0.toByte()) ) break@loop
                        }
                    }
                    list[list.size-1] = (list[list.size -1]*2 + (constructedBit and 1)).toByte()
                    if(bit == 0) bit = 8
                    bit--
                }
            }
            list.removeLast()
            list.removeLast()
            list.removeLast()
            val arrayPassword = password.encodeToByteArray()
            val arrayByte: ByteArray = list.toByteArray()
            for(i in arrayByte.indices){
                arrayByte[i] = (arrayByte[i].toInt() xor arrayPassword[i % arrayPassword.size].toInt()).toByte()
            }
            val answer: String = arrayByte.toString(Charsets.UTF_8)
            return answer
        }
        fun saveImage(path: File){
            ImageIO.write(image, "png", path)
        }

    }

    fun main() {
        while(true){
            println("Task (hide, show, exit): ")
            val commandList : List<String> = readln().split(" ")
            when(commandList[0]){
                "hide" -> {
                    println("Input image file:")
                    val stringInput = readln()
                    val inputFile = File(stringInput)
                    println("Output image file:")
                    val stringOutput = readln()
                    val outputFile = File(stringOutput)
                    println("Message to hide:")
                    val message = readln()
                    println("Password:")
                    val password = readln()
                    if(!inputFile.exists()){
                        println("Can't read input file!")
                        continue
                    }
                    val photo = ImageChanger(inputFile)
                    try {
                        photo.encryptImage(message, password)
                    }
                    catch (e: java.lang.Exception){
                        println(e.message)
                        continue
                    }
                    photo.saveImage(outputFile)
                    println("Message saved in $outputFile image")
                }
                "show" -> {
                    println("Input image file:")
                    val stringImagePath = readln()
                    val imagePath = File(stringImagePath)
                    println("Password:")
                    val password = readln()
                    if(!imagePath.exists()){
                        println("Can't read input file!")
                        continue
                    }
                    val photo = ImageChanger(imagePath)
                    println("Message:")
                    println(photo.decryptImage(password))
                }
                "exit" ->{
                    println("Bye!")
                    break
                }
                else -> println("Wrong task: ${commandList[0]}")
            }
        }
    }

