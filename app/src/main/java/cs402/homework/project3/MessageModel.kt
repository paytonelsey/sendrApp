package cs402.homework.project3

/**
 * Model for storing a message in the database. Stores the email, message, subject line, date and
 * time, location, any attached images.
 * @author Payton Elsey
 */
data class MessageModel(
    var email: String = "",
    var message: String = "",
    var subject: String = "",
    var datetime: String = "",
    var location: String = "",
    var files: ByteArray? = null
) {
    /**
     * Auto generated for ByteArray files
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageModel

        if (files != null) {
            if (other.files == null) return false
            if (!files!!.contentEquals(other.files!!)) return false
        } else if (other.files != null) return false

        return true
    }

    /**
     * Auto generated for ByteArray files
     */
    override fun hashCode(): Int {
        return files?.contentHashCode() ?: 0
    }
}