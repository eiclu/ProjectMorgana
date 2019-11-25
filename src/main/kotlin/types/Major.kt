package types

enum class Major(val roleName: String, val majorName: String) {
    B033_526("Wirtschaftsinformatik", "Bachelor: Business Informatics"),
    B033_535("Technische Informatik", "Bachelor: Computer Engineering"),
    B033_532("Medieninformatik", "Bachelor: Media Informatics"),
    B033_533("Medizinische Informatik", "Bachelor: Medical Informatics"),
    B033_534("Software Engineering", "Bachelor: Software Engineering"),
    M066_926("Master", "Master: Business Informatics"),
    M066_931("Master", "Master: Logic and Computation Computational Intelligence"),
    M066_938("Master", "Master: Computer Engineering"),
    M066_935("Master", "Master: Media Informatics"),
    M066_936("Master", "Master: Medical Informatics"),
    M066_937("Master", "Master: Software Engineering Internet Computing"),
    M066_932("Master", "Master: Visual Computing"),
    M066_011("Master", "Master: Computational Logic"),
    M066_645("Master", "Master: Data Science"),
    M066_950("Master", "Master: Didactics of Informatics");

    companion object {
        fun getByName(majorName: String): Major? = enumValues<Major>().find { it.roleName == majorName || it.majorName == majorName }

        fun String?.major(): Major? = if (this == null) null else Major.valueOf(this)
    }
}

/*
        Triple("033 526", "B_Business_Informatics", 6),
        Triple("033 535", "B_Computer_Engineering", 6),
        Triple("033 532", "B_Media_Informatics", 6),
        Triple("033 533", "B_Medical_Informatics", 6),
        Triple("033 534", "B_Software_Engineering", 6),
        Triple("066 926", "M_Business_Informatics", 4),
        Triple("066 931", "M_Logic_and_Computation_Computational_Intelligence", 4),
        Triple("066 938", "M_Computer_Engineering", 4),
        Triple("066 935", "M_Media_Informatics", 4),
        Triple("066 936", "M_Medical_Informatics", 4),
        Triple("066 937", "M_Software_Engineering_Internet_Computing", 4),
        Triple("066 932", "M_Visual_Computing", 4),
        Triple("066 011", "M_Computational_Logic", 4),
        Triple("066 950", "M_Didactics_of_Informatics", 4)
        * */