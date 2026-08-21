package com.fitness.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户业务编码生成器单元测试
 */
class UserCodeGeneratorTest {

    @Test
    @DisplayName("格式：user- 前缀 + 15 位随机值")
    void generate_hasCorrectFormat() {
        String code = UserCodeGenerator.generate();

        assertThat(code).startsWith("user-");
        assertThat(code).hasSize(20); // user-(5) + 15
    }

    @Test
    @DisplayName("字符集：不含易混淆字符 0/O/1/I/l")
    void generate_excludesConfusingChars() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            codes.add(UserCodeGenerator.generate());
        }
        String joined = String.join("", codes);

        assertThat(joined).doesNotContain("0", "O", "1", "I", "l");
    }

    @Test
    @DisplayName("唯一性：批量生成不重复")
    void generate_isUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(UserCodeGenerator.generate());
        }
        assertThat(codes).hasSize(1000);
    }
}
