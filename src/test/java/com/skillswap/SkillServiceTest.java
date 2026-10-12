package com.skillswap;

import com.skillswap.model.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SkillServiceTest {

    @Test
    @DisplayName("Skill model should normalize types and proficiencies")
    void testSkillModelNormalization() {
        Skill skill = new Skill(1, " Java ", "teach", "intermediate", "Backend programming");

        assertEquals(1, skill.getStudentId());
        assertEquals(" Java ", skill.getSkillName());
        assertEquals(Skill.TYPE_TEACH, skill.getSkillType());
        assertEquals(Skill.LEVEL_INTERMEDIATE, skill.getProficiencyLevel());
    }

    @Test
    @DisplayName("Skill equality based on skillId")
    void testSkillEquality() {
        Skill s1 = new Skill(10, 1, "Java", "TEACH", "ADVANCED", "desc");
        Skill s2 = new Skill(10, 2, "Python", "LEARN", "BEGINNER", "other");
        Skill s3 = new Skill(11, 1, "Java", "TEACH", "ADVANCED", "desc");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    @Test
    @DisplayName("Valid skill types should match TEACH and LEARN constants")
    void testValidSkillTypes() {
        assertTrue(Skill.TYPE_TEACH.equalsIgnoreCase("TEACH"));
        assertTrue(Skill.TYPE_LEARN.equalsIgnoreCase("LEARN"));
        assertFalse(Skill.TYPE_TEACH.equalsIgnoreCase("HOBBY"));
    }

    @Test
    @DisplayName("Valid proficiency levels should match BEGINNER, INTERMEDIATE, ADVANCED")
    void testValidProficiencyLevels() {
        assertTrue(Skill.LEVEL_BEGINNER.equalsIgnoreCase("BEGINNER"));
        assertTrue(Skill.LEVEL_INTERMEDIATE.equalsIgnoreCase("INTERMEDIATE"));
        assertTrue(Skill.LEVEL_ADVANCED.equalsIgnoreCase("ADVANCED"));
        assertFalse(Skill.LEVEL_ADVANCED.equalsIgnoreCase("MASTER"));
    }
}
