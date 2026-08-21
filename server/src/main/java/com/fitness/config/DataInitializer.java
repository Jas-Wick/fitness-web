package com.fitness.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fitness.common.util.UserCodeGenerator;
import com.fitness.entity.ExerciseEntity;
import com.fitness.entity.UserEntity;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 数据初始化：创建默认管理员账号与健身动作库种子数据（均幂等，避免 SQL 硬编码哈希 / 重复插入）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final ExerciseMapper exerciseMapper;
    private final PasswordEncoder passwordEncoder;

    /** 管理员初始密码，由配置注入（prod 必须显式配置，否则不创建管理员） */
    @Value("${fitness.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        initAdmin();
        initExercises();
    }

    /** 创建默认管理员账号：密码不写死、不打日志 */
    private void initAdmin() {
        Long count = userMapper.selectCount(new QueryWrapper<UserEntity>().eq("username", "admin"));
        if (count != null && count > 0) {
            return;
        }
        if (!StringUtils.hasText(adminPassword)) {
            log.error("未配置 fitness.admin.password，跳过默认管理员初始化");
            return;
        }
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setUserCode(UserCodeGenerator.generate());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        userMapper.insert(admin);
        log.info("已创建默认管理员账号：admin");
    }

    /** 动作 → SVG 静态资源文件名（client/public/exercises/ 下，随前端仓库提交） */
    private static final Map<String, String> EXERCISE_SLUGS = Map.ofEntries(
            Map.entry("杠铃卧推", "barbell-bench-press"),
            Map.entry("上斜杠铃卧推", "incline-barbell-bench-press"),
            Map.entry("哑铃卧推", "dumbbell-bench-press"),
            Map.entry("上斜哑铃卧推", "incline-dumbbell-bench-press"),
            Map.entry("哑铃飞鸟", "dumbbell-fly"),
            Map.entry("绳索夹胸", "cable-crossover"),
            Map.entry("双杠臂屈伸", "dips"),
            Map.entry("俯卧撑", "push-up"),
            Map.entry("引体向上", "pull-up"),
            Map.entry("高位下拉", "lat-pulldown"),
            Map.entry("坐姿划船", "seated-cable-row"),
            Map.entry("杠铃俯身划船", "barbell-bent-over-row"),
            Map.entry("单臂哑铃划船", "one-arm-dumbbell-row"),
            Map.entry("硬拉", "deadlift"),
            Map.entry("直臂下拉", "straight-arm-pulldown"),
            Map.entry("哑铃推举", "dumbbell-shoulder-press"),
            Map.entry("杠铃肩上推举", "barbell-overhead-press"),
            Map.entry("哑铃侧平举", "dumbbell-lateral-raise"),
            Map.entry("哑铃前平举", "dumbbell-front-raise"),
            Map.entry("俯身飞鸟", "bent-over-lateral-raise"),
            Map.entry("面拉", "face-pull"),
            Map.entry("杠铃深蹲", "barbell-squat"),
            Map.entry("颈前深蹲", "front-squat"),
            Map.entry("箭步蹲", "lunge"),
            Map.entry("腿举", "leg-press"),
            Map.entry("坐姿腿屈伸", "leg-extension"),
            Map.entry("俯卧腿弯举", "lying-leg-curl"),
            Map.entry("站姿提踵", "standing-calf-raise"),
            Map.entry("罗马尼亚硬拉", "romanian-deadlift"),
            Map.entry("哑铃弯举", "dumbbell-curl"),
            Map.entry("杠铃弯举", "barbell-curl"),
            Map.entry("锤式弯举", "hammer-curl"),
            Map.entry("绳索下压", "cable-triceps-pushdown"),
            Map.entry("仰卧臂屈伸", "skull-crusher"),
            Map.entry("窄握卧推", "close-grip-bench-press"),
            Map.entry("平板支撑", "plank"),
            Map.entry("卷腹", "crunch"),
            Map.entry("反向卷腹", "reverse-crunch"),
            Map.entry("俄罗斯转体", "russian-twist"),
            Map.entry("登山者", "mountain-climber"),
            Map.entry("悬垂举腿", "hanging-leg-raise"),
            Map.entry("侧平板支撑", "side-plank"));

    /** 初始化健身动作库种子数据：表为空时才写入，避免每次启动重复插入 */
    private void initExercises() {
        Long count = exerciseMapper.selectCount(new QueryWrapper<ExerciseEntity>());
        if (count != null && count > 0) {
            return;
        }
        List<ExerciseEntity> seeds = List.of(
                // 胸部
                seed("杠铃卧推", "胸部", "发展胸大肌的经典复合动作，是上肢推力的核心训练。",
                        "1.仰卧于平板凳，双脚踩实地面；\n2.双手略宽于肩握杠，缓慢下放至胸部；\n3.用胸部发力将杠铃推起至手臂伸直。",
                        "肩胛骨收紧下沉；下放时控制速度；大重量需有人保护。", 1),
                seed("上斜杠铃卧推", "胸部", "针对胸大肌上部与三角肌前束，塑造上胸轮廓。",
                        "1.调节凳面至30~45度上斜；\n2.握距略宽于肩，杠铃下放至锁骨上方；\n3.胸部发力推起至手臂伸直。",
                        "下放不要弹胸；背部贴紧凳面。", 2),
                seed("哑铃卧推", "胸部", "增加动作幅度，均衡发展两侧胸大肌。",
                        "1.仰卧平板凳，双手持哑铃于胸部两侧；\n2.向上推起至两臂伸直、哑铃靠拢；\n3.缓慢下放至胸部有拉伸感。",
                        "手腕保持中立；下放控制在可控速度。", 3),
                seed("上斜哑铃卧推", "胸部", "强化上胸与肩前束，改善上胸饱满度。",
                        "1.凳面调至30度上斜，双手持哑铃；\n2.向上推起至两臂伸直；\n3.控制下放至胸部两侧。",
                        "肩胛骨后缩下沉，避免耸肩。", 4),
                seed("哑铃飞鸟", "胸部", "利用胸大肌水平内收功能，拉伸胸肌中缝。",
                        "1.仰卧，双臂持哑铃向两侧打开；\n2.肘部微屈，以弧形轨迹向上合拢；\n3.至顶点顶峰收缩后缓慢下放。",
                        "肘部角度固定，避免用手臂发力。", 5),
                seed("绳索夹胸", "胸部", "持续张力刺激胸肌，塑造胸肌中缝。",
                        "1.站于龙门架中间，双手握绳索把手；\n2.身体前倾，双臂向体前合拢；\n3.顶峰收缩后缓慢还原。",
                        "保持身体稳定，不要后仰借力。", 6),
                seed("双杠臂屈伸", "胸部", "自重训练胸大肌下沿与肱三头肌。",
                        "1.双手撑双杠，身体前倾；\n2.屈肘下放至胸部有拉伸感；\n3.胸部发力撑起至手臂伸直。",
                        "身体前倾练胸、直立练三头；肩部不适者慎做。", 7),
                seed("俯卧撑", "胸部", "经典自重推类动作，适合任何场地。",
                        "1.双手略宽于肩撑地，身体成直线；\n2.屈肘下放至胸部接近地面；\n3.胸部发力推起身体。",
                        "核心收紧，不塌腰不撅臀。", 8),
                // 背部
                seed("引体向上", "背部", "锻炼背阔肌与肱二头肌的自重王牌动作。",
                        "1.双手正握单杠，略宽于肩；\n2.收紧核心，背部发力将身体拉起至下巴过杠；\n3.缓慢下放至手臂接近伸直。",
                        "避免摆动借力；肩部不要耸起；下放时保持控制。", 9),
                seed("高位下拉", "背部", "发展背阔肌宽度，是引体向上的替代动作。",
                        "1.坐姿固定腿部，双手宽握横杆；\n2.背部发力将横杆拉至锁骨上方；\n3.缓慢还原至手臂伸直。",
                        "肩胛骨下沉，避免用手臂猛拉。", 10),
                seed("坐姿划船", "背部", "发展上中背部厚度，改善体态。",
                        "1.坐姿，双手握把手，膝盖微屈；\n2.将把手拉向腹部，挺胸收腹；\n3.缓慢还原至手臂伸直。",
                        "背部挺直，肩胛骨向脊柱靠拢。", 11),
                seed("杠铃俯身划船", "背部", "复合动作发展上背与背阔肌厚度。",
                        "1.俯身约45度，双手与肩同宽握杠；\n2.将杠铃拉向腹部；\n3.缓慢下放至手臂伸直。",
                        "背部保持中立，避免弓背。", 12),
                seed("单臂哑铃划船", "背部", "单侧孤立训练背阔肌，改善左右不平衡。",
                        "1.单膝跪凳，同侧手撑凳，另一手持哑铃；\n2.将哑铃拉向腰侧；\n3.缓慢下放至手臂伸直。",
                        "躯干稳定，不要旋转借力。", 13),
                seed("硬拉", "背部", "全身复合动作，强化后链与背部力量。",
                        "1.双脚与髋同宽，俯身握杠；\n2.挺胸收紧核心，蹬地起身；\n3.杠铃沿腿前侧缓慢下放。",
                        "全程保持背部中立；核心收紧。", 14),
                seed("直臂下拉", "背部", "孤立刺激背阔肌，作为背部训练的收尾。",
                        "1.站姿面向龙门架，双手握直杆；\n2.直臂将杆向下拉至大腿前；\n3.缓慢还原至肩高。",
                        "手臂微屈不锁死，用背发力。", 15),
                // 肩部
                seed("哑铃推举", "肩部", "强化三角肌前中束，改善肩部力量与形态。",
                        "1.坐姿或站姿，双手持哑铃置于肩部两侧；\n2.向上推举至手臂伸直；\n3.缓慢下放回起始位置。",
                        "腰背挺直，避免过度后仰；选用可控重量。", 16),
                seed("杠铃肩上推举", "肩部", "发展三角肌前中束与上背稳定的推举动作。",
                        "1.站姿或坐姿，杠铃置于锁骨前；\n2.向上推举至头顶上方；\n3.缓慢下放至锁骨位置。",
                        "核心收紧，避免腰椎过度反弓。", 17),
                seed("哑铃侧平举", "肩部", "孤立发展三角肌中束，增加肩部宽度。",
                        "1.站姿双手持哑铃自然下垂；\n2.双臂向两侧平举至与肩同高；\n3.缓慢下放。",
                        "肘部微屈，避免耸肩借力。", 18),
                seed("哑铃前平举", "肩部", "孤立发展三角肌前束。",
                        "1.双手持哑铃置于大腿前；\n2.交替或同时将哑铃前举至肩高；\n3.缓慢下放。",
                        "身体不要后仰借力。", 19),
                seed("俯身飞鸟", "肩部", "强化三角肌后束，改善圆肩体态。",
                        "1.俯身约45度，双手持哑铃下垂；\n2.双臂向两侧打开至与肩同高；\n3.缓慢下放。",
                        "肘部微屈，用后束发力而非手臂。", 20),
                seed("面拉", "肩部", "强化三角肌后束与肩袖，保护肩关节。",
                        "1.龙门架装绳索，双手握绳两端；\n2.将绳索拉向面部，肘部抬高；\n3.顶峰收缩后缓慢还原。",
                        "用轻重量，注重肩胛骨后缩。", 21),
                // 腿部
                seed("杠铃深蹲", "腿部", "发展股四头肌、臀部与核心的全身性力量动作。",
                        "1.杠铃置于斜方肌上，双脚与肩同宽；\n2.屈髋屈膝下蹲至大腿与地面平行；\n3.脚掌发力站起回到起始位置。",
                        "膝盖方向与脚尖一致；背部保持中立。", 22),
                seed("颈前深蹲", "腿部", "强化股四头肌与核心，动作更垂直。",
                        "1.杠铃置于锁骨前，双手交叉扶杠；\n2.保持躯干直立下蹲；\n3.蹬地站起。",
                        "肘部抬高，核心收紧，避免前倾。", 23),
                seed("箭步蹲", "腿部", "单侧训练腿部力量与平衡，改善左右差异。",
                        "1.站姿，向前跨一大步；\n2.下蹲至前后膝均约90度；\n3.前腿发力蹬回起始位置。",
                        "前膝不要超过脚尖过多；躯干直立。", 24),
                seed("腿举", "腿部", "器械安全地发展腿部整体力量。",
                        "1.坐于腿举机，双脚与肩同宽踩踏板；\n2.解锁后屈膝下放；\n3.蹬起至腿接近伸直。",
                        "膝盖不要锁死；下放深度适度。", 25),
                seed("坐姿腿屈伸", "腿部", "孤立发展股四头肌。",
                        "1.坐于器械，脚踝卡于滚垫后；\n2.伸膝抬起小腿至腿伸直；\n3.缓慢下放。",
                        "顶点可停顿挤压，不要甩动。", 26),
                seed("俯卧腿弯举", "腿部", "孤立发展腘绳肌（大腿后侧）。",
                        "1.俯卧于器械，脚踝卡于滚垫下；\n2.屈膝将滚垫向臀部卷起；\n3.缓慢还原。",
                        "髋部贴紧凳面，不要抬起借力。", 27),
                seed("站姿提踵", "腿部", "发展小腿腓肠肌。",
                        "1.前脚掌踩于台阶边缘，脚跟悬空；\n2.踮起脚尖至最高点；\n3.缓慢下放脚跟至拉伸感。",
                        "动作幅度完整，节奏控制。", 28),
                seed("罗马尼亚硬拉", "腿部", "以髋铰链为主，强化臀腿与腘绳肌。",
                        "1.双手握杠铃站直，膝盖微屈；\n2.以髋为轴后推臀部，杠铃沿腿下放；\n3.挺髋站起。",
                        "背部全程中立，感受大腿后侧拉伸。", 29),
                // 手臂
                seed("哑铃弯举", "手臂", "孤立锻炼肱二头肌的经典动作。",
                        "1.双手持哑铃自然下垂，掌心向前；\n2.以肘为轴弯举至收缩位；\n3.缓慢下放至手臂伸直。",
                        "身体不要晃动借力；肘部贴紧身体两侧。", 30),
                seed("杠铃弯举", "手臂", "使用较大重量发展肱二头肌整体。",
                        "1.双手与肩同宽反握杠铃；\n2.弯举至前臂接近垂直；\n3.缓慢下放。",
                        "保持躯干稳定，不后仰借力。", 31),
                seed("锤式弯举", "手臂", "侧重肱肌与前臂，增加手臂围度。",
                        "1.双手持哑铃，掌心相对；\n2.以中立握弯举至收缩位；\n3.缓慢下放。",
                        "肘部固定，保持中立握。", 32),
                seed("绳索下压", "手臂", "孤立发展肱三头肌。",
                        "1.面向龙门架，双手握绳索；\n2.肘部贴紧身体，向下压至手臂伸直；\n3.缓慢还原。",
                        "肘部固定不动，用三头发力。", 33),
                seed("仰卧臂屈伸", "手臂", "发展肱三头肌长头。",
                        "1.仰卧，双手窄握曲杠于额头上方；\n2.屈肘下放杠铃至额前；\n3.伸肘还原。",
                        "肘部指向固定，避免外张。", 34),
                seed("窄握卧推", "手臂", "复合动作发展肱三头肌与胸部内侧。",
                        "1.仰卧，双手与肩同宽或更窄握杠；\n2.下放杠铃至下胸；\n3.推起至手臂伸直。",
                        "肘部贴近身体，控制下放。", 35),
                // 核心
                seed("平板支撑", "核心", "强化腹横肌与核心稳定性的静态训练。",
                        "1.肘撑地面，身体呈一条直线；\n2.收紧腹部与臀部，保持呼吸；\n3.坚持规定时长后缓慢放下。",
                        "臀部不要塌陷或抬起；保持颈部中立。", 36),
                seed("卷腹", "核心", "基础腹直肌训练，相比仰卧起坐更护腰。",
                        "1.仰卧屈膝，双手扶头或抱胸；\n2.用腹肌卷起上背部；\n3.缓慢下放还原。",
                        "下背贴地，不要用手拉脖子。", 37),
                seed("反向卷腹", "核心", "侧重下腹部训练。",
                        "1.仰卧，双腿抬起屈膝90度；\n2.用下腹将臀部卷离地面；\n3.缓慢下放。",
                        "动作慢而稳，避免甩腿借力。", 38),
                seed("俄罗斯转体", "核心", "发展腹斜肌与核心旋转力量。",
                        "1.坐姿，上身微微后仰，双脚抬起；\n2.双手持重物左右转体；\n3.保持核心收紧。",
                        "背部挺直，转动来自躯干而非手臂。", 39),
                seed("登山者", "核心", "动态核心训练，兼顾心肺。",
                        "1.俯撑姿势，身体成直线；\n2.交替将膝盖向胸口收；\n3.保持节奏。",
                        "臀部不要抬高，核心收紧。", 40),
                seed("悬垂举腿", "核心", "进阶下腹训练，强化握力与核心。",
                        "1.双手悬垂于单杠；\n2.用下腹将双腿抬起至与地面平行或更高；\n3.缓慢下放。",
                        "避免摆动借力，控制下放。", 41),
                seed("侧平板支撑", "核心", "强化腹斜肌与侧向核心稳定。",
                        "1.侧卧，前臂撑地，身体成直线；\n2.抬起髋部保持；\n3.坚持后换边。",
                        "髋部不要下沉，保持直线。", 42)
        );
        for (ExerciseEntity seed : seeds) {
            exerciseMapper.insert(seed);
        }
        log.info("已初始化 {} 条健身动作种子数据", seeds.size());
    }

    private ExerciseEntity seed(String name, String bodyPart, String description,
                                String steps, String precautions, int sortOrder) {
        ExerciseEntity e = new ExerciseEntity();
        e.setName(name);
        e.setBodyPart(bodyPart);
        e.setDescription(description);
        e.setSteps(steps);
        e.setPrecautions(precautions);
        e.setViewCount(0);
        e.setStatus(1);
        e.setSortOrder(sortOrder);
        String slug = EXERCISE_SLUGS.get(name);
        if (slug != null) {
            e.setImageUrl("/exercises/" + slug + ".svg");
        }
        return e;
    }
}
