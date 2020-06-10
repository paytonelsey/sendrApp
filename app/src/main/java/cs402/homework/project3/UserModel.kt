package cs402.homework.project3

/**
 * Model for storing a user in the database. Stores the email, first name, last name, password,
 * and profile picture.
 * @author Payton Elsey
 */
data class UserModel(

    var email: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var password: String = "",
    var profilePic: ByteArray? = null
) {

    /**
     * Auto generated for ByteArray profilePic
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserModel

        if (profilePic != null) {
            if (other.profilePic == null) return false
            if (!profilePic!!.contentEquals(other.profilePic!!)) return false
        } else if (other.profilePic != null) return false

        return true
    }

    /**
     * Auto generated for ByteArray profilePic
     */
    override fun hashCode(): Int {
        return profilePic?.contentHashCode() ?: 0
    }
}