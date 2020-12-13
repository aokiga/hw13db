import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.date

object Faculty: Table("faculty") {
    val facultyId = integer("faculty_id").autoIncrement().uniqueIndex()
    val facultyName = text("faculty_name").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(facultyId)
}

object Course: Table("course") {
    val courseId = integer("course_id").autoIncrement().uniqueIndex()
    val courseName = text("course_name").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(courseId)
}

object Teacher: Table("teacher") {
    val teacherId = integer("teacher_id").autoIncrement().uniqueIndex()
    val lastName = text("last_name")
    val firstName = text("first_name")
    val birthday = date("birthday").nullable()
    val curatorId = integer("curator_id").references(teacherId)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(teacherId)
}

object Student: Table("student") {
    val studentId = integer("student_id").autoIncrement().uniqueIndex()
    val studentNumber = text("student_number")
    val lastName = text("last_name")
    val firstName = text("first_name")
    val gender = integer("gender")
    val birthday = date("birthday").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(studentId)
}

object Unit: Table("unit") {
    val unitId = integer("unit_id").autoIncrement().uniqueIndex()
    val unitNumber = text("unit_number").nullable()
    val facultyId = integer("faculty_id").references(Faculty.facultyId)

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(unitId)
}

object StudentUnit: Table("student_unit") {
    val studentUnitId = integer("student_unit_id").autoIncrement().uniqueIndex()
    val unitId = integer("unit_id").references(Unit.unitId)
    val studentId = integer("student_id").references(Student.studentId)
    val isHead = integer("is_head")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(studentUnitId)
}

object TeacherCourseFaculty: Table("teacher_course_faculty") {
    val teacherCourseFacultyId = integer("teacher_course_faculty_id").autoIncrement().uniqueIndex()
    val teacherId = integer("teacher_id").references(Teacher.teacherId)
    val courseId = integer("course_id").references(Course.courseId)
    val facultyId = integer("faculty_id").references(Faculty.facultyId)
    val duration = double("duration").nullable()

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(teacherCourseFacultyId)
}

object StudentGrade: Table("student_grade") {
    val studentGradeId = integer("student_grade_id").autoIncrement().uniqueIndex()
    val studentId = integer("student_id").references(Student.studentId)
    val courseId = integer("course_id").references(Course.courseId)
    val grade = integer("grade")

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(studentGradeId)
}