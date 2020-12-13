import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class StudentName(val lastName: String, val firstName: String) {
    override fun toString(): String {
        return "$firstName $lastName"
    }
}

fun getCourses() : List<String?> =
    transaction {
        Course.selectAll().orderBy(Course.courseName).map { it[Course.courseName] }.toList()
    }

fun getStudents() : List<StudentName> =
    transaction {
        Student.selectAll().map { StudentName(it[Student.firstName], it[Student.lastName]) }.toList()
    }

fun getCourseId(courseName: String) : Int? =
    transaction {
        Course.select { Course.courseName eq courseName }.map { it[Course.courseId] }.firstOrNull()
    }

fun getStudentId(lastName: String) : Int? =
    transaction {
        Student.select { Student.lastName eq lastName }.map { it[Student.studentId] }.firstOrNull()
    }

fun getStudents(courseName : String) : List<Pair<StudentName, Int>> {
    val courseId = getCourseId(courseName) ?: return emptyList()
    return transaction {
        StudentGrade.select {
            StudentGrade.courseId eq courseId
        }.map {
            val student = Student.select { Student.studentId eq it[StudentGrade.studentId] }.first()
            StudentName(student[Student.lastName], student[Student.firstName]) to it[StudentGrade.grade]
        }
    }
}

fun getStudentGradeId(courseId : Int, studentId : Int) : Int? =
    transaction {
        StudentGrade.select {
            (StudentGrade.courseId eq courseId) and (StudentGrade.studentId eq studentId)
        }.map { it[StudentGrade.studentGradeId] }.firstOrNull()
    }

fun getGrade(courseName : String, studentLastName : String) : Int? {
    val courseId = getCourseId(courseName) ?: return null
    val studentId = getStudentId(studentLastName) ?: return null
    return transaction {
        StudentGrade.select {
            (StudentGrade.courseId eq courseId) and (StudentGrade.studentId eq studentId)
        }.map { it[StudentGrade.grade] }.firstOrNull()
    }
}

fun setGrade(courseName: String, studentLastName: String, grade : Int) {
    val courseId = getCourseId(courseName) ?: return
    val studentId = getStudentId(studentLastName) ?: return
    val studentGradeId = getStudentGradeId(courseId, studentId)
    if (studentGradeId == null) {
        transaction {
            StudentGrade.insert {
                it[StudentGrade.courseId] = courseId
                it[StudentGrade.studentId] = studentId
                it[StudentGrade.grade] = grade
            }
        }
    } else {
        transaction {
            StudentGrade.update(
                where = {
                    StudentGrade.studentGradeId eq studentGradeId
                }
            ) {
                it[StudentGrade.grade] = grade
            }
        }
    }
}

fun main(args: Array<String>) {
    Database.connect(UserConfig.url, driver = "org.postgresql.Driver",
        user = UserConfig.user, password = UserConfig.password)
    if (args.isEmpty()) {
        println("Error: Not enough arguments")
        return
    }
    when (args[0]) {
        "c" -> getCourses().forEach { println(it) }
        "s" -> getStudents().forEach { studentName -> println(studentName) }
        "g" -> {
            if (args.size < 2) {
                println("Error: Not enough arguments")
                return
            }
            if (args.size == 2) {
                val courseName = args[1]
                getStudents(courseName).forEach {
                    val studentName = it.first
                    val grade = it.second
                    println("$studentName: $grade")
                }
            } else {
                val courseName = args[1]
                val studentLastName = args[2]
                println(getGrade(courseName, studentLastName) ?: "Error: Grade not found")
            }
        }
        "u" -> {
            if (args.size < 4) {
                println("Error: Not enough arguments")
                return
            }
            val courseName = args[1]
            val studentLastName = args[2]
            val grade = args[3].toIntOrNull()
            if (grade == null) {
                println("Error: Invalid input")
                return
            }
            setGrade(courseName, studentLastName, grade)
        }
    }
}