package com.esmile.edu.biz;

import com.esmile.edu.common.exception.AuthorizationException;
import com.esmile.edu.common.exception.InsufficientPermissionsException;
import com.esmile.edu.common.exception.course.AlreadyEnrolledException;
import com.esmile.edu.common.exception.course.ChapterNotFoundException;
import com.esmile.edu.common.exception.course.CourseNotFoundException;
import com.esmile.edu.common.exception.course.CourseNotPublishedException;
import com.esmile.edu.common.exception.course.LessonNotFoundException;
import com.esmile.edu.dto.request.CreateChapterRequest;
import com.esmile.edu.dto.request.CreateCourseRequest;
import com.esmile.edu.dto.request.CreateLessonRequest;
import com.esmile.edu.dto.request.UpdateChapterRequest;
import com.esmile.edu.dto.request.UpdateCourseRequest;
import com.esmile.edu.dto.request.UpdateLessonRequest;
import com.esmile.edu.dto.response.*;
import com.esmile.edu.module.course.*;
import com.esmile.edu.module.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseBizService {
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public CourseBizService(
            CourseRepository courseRepository,
            ChapterRepository chapterRepository,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.chapterRepository = chapterRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    private record EducatorInfo(String name, String avatar) {}

    private EducatorInfo getEducatorInfo(Long educatorId) {
        return userRepository.findById(educatorId)
            .map(u -> new EducatorInfo(u.getNickname(), u.getAvatar()))
            .orElse(new EducatorInfo("未知讲师", null));
    }

    private String getEducatorName(Long educatorId) {
        return getEducatorInfo(educatorId).name();
    }

    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, Long educatorId) {
        CourseEntity course = new CourseEntity(
            request.title(),
            request.description(),
            educatorId,
            request.cover()
        );
        return CourseResponse.from(courseRepository.save(course), getEducatorName(educatorId));
    }

    @Transactional
    public CourseResponse publishCourse(Long courseId, Long educatorId) {
        CourseEntity course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new InsufficientPermissionsException("无权限操作此课程");
        }
        course.publish();
        return CourseResponse.from(courseRepository.save(course), getEducatorName(educatorId));
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId, Long educatorId) {
        CourseEntity course = courseRepository.findByIdAndEducatorId(courseId, educatorId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByPosition(courseId);
        List<ChapterResponse> chapterResponses = chapters.stream()
            .map(ch -> {
                List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByPosition(ch.getId());
                return ChapterResponse.from(ch, lessons.stream().map(LessonResponse::from).toList());
            })
            .toList();
        return CourseResponse.from(course, getEducatorName(course.getEducatorId()), chapterResponses);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, Long educatorId, UpdateCourseRequest request) {
        CourseEntity course = courseRepository.findByIdAndEducatorId(courseId, educatorId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCover(request.cover());
        return CourseResponse.from(courseRepository.save(course), getEducatorName(educatorId));
    }

    @Transactional
    public void deleteCourse(Long courseId, Long educatorId) {
        CourseEntity course = courseRepository.findByIdAndEducatorId(courseId, educatorId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long courseId) {
        CourseEntity course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        List<ChapterEntity> chapters = chapterRepository.findByCourseIdOrderByPosition(courseId);
        List<ChapterResponse> chapterResponses = chapters.stream()
            .map(ch -> {
                List<LessonEntity> lessons = lessonRepository.findByChapterIdOrderByPosition(ch.getId());
                return ChapterResponse.from(ch, lessons.stream().map(LessonResponse::from).toList());
            })
            .toList();
        EducatorInfo educatorInfo = getEducatorInfo(course.getEducatorId());
        return CourseDetailResponse.from(course, educatorInfo.name(), educatorInfo.avatar(), chapterResponses);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> listCourses(Pageable pageable) {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED, pageable)
            .map(course -> CourseResponse.from(
                course,
                getEducatorName(course.getEducatorId()),
                (int) chapterRepository.countByCourseId(course.getId()),
                (int) lessonRepository.countByCourseId(course.getId())
            ));
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> listCoursesByEducator(Long educatorId, Pageable pageable) {
        String educatorName = getEducatorName(educatorId);
        return courseRepository.findByEducatorId(educatorId, pageable)
            .map(course -> CourseResponse.from(course, educatorName));
    }

    @Transactional
    public ChapterResponse createChapter(CreateChapterRequest request) {
        ChapterEntity chapter = new ChapterEntity(
            request.title(),
            request.courseId(),
            request.position()
        );
        return ChapterResponse.from(chapterRepository.save(chapter), List.of());
    }

    @Transactional
    public ChapterResponse updateChapter(Long chapterId, Long educatorId, UpdateChapterRequest request) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ChapterNotFoundException(chapterId));
        CourseEntity course = courseRepository.findById(chapter.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(chapter.getCourseId()));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new InsufficientPermissionsException("无权限操作此课程");
        }
        chapter.setTitle(request.title());
        chapter.setPosition(request.position());
        return ChapterResponse.from(chapterRepository.save(chapter), List.of());
    }

    @Transactional
    public void deleteChapter(Long chapterId, Long educatorId) {
        ChapterEntity chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new ChapterNotFoundException(chapterId));
        CourseEntity course = courseRepository.findById(chapter.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(chapter.getCourseId()));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new InsufficientPermissionsException("无权限操作此课程");
        }
        chapterRepository.delete(chapter);
    }

    @Transactional
    public LessonResponse createLesson(CreateLessonRequest request) {
        ChapterEntity chapter = chapterRepository.findById(request.chapterId())
            .orElseThrow(() -> new ChapterNotFoundException(request.chapterId()));
        CourseEntity course = courseRepository.findById(chapter.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(chapter.getCourseId()));
        LessonEntity lesson = new LessonEntity(
            request.title(),
            request.chapterId(),
            chapter.getCourseId(),
            request.position()
        );
        return LessonResponse.from(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, Long educatorId, UpdateLessonRequest request) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new LessonNotFoundException(lessonId));
        CourseEntity course = courseRepository.findById(lesson.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(lesson.getCourseId()));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new InsufficientPermissionsException("无权限操作此课程");
        }
        lesson.setTitle(request.title());
        lesson.setPosition(request.position());
        return LessonResponse.from(lessonRepository.save(lesson));
    }

    @Transactional
    public void deleteLesson(Long lessonId, Long educatorId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new LessonNotFoundException(lessonId));
        CourseEntity course = courseRepository.findById(lesson.getCourseId())
            .orElseThrow(() -> new CourseNotFoundException(lesson.getCourseId()));
        if (!course.getEducatorId().equals(educatorId)) {
            throw new InsufficientPermissionsException("无权限操作此课程");
        }
        lessonRepository.delete(lesson);
    }

    public LessonResponse getLessonById(Long lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new LessonNotFoundException(lessonId));
        return LessonResponse.from(lesson);
    }

    @Transactional
    public void enrollCourse(Long userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AlreadyEnrolledException();
        }
        CourseEntity course = courseRepository.findById(courseId)
            .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new CourseNotPublishedException();
        }
        EnrollmentEntity enrollment = new EnrollmentEntity(userId, courseId);
        enrollmentRepository.save(enrollment);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listEnrolledCourses(Long userId) {
        // 1. Only query course IDs (single query)
        List<Long> courseIds = enrollmentRepository.findCourseIdsByUserId(userId);
        if (courseIds.isEmpty()) {
            return List.of();
        }

        // 2. Batch query courses and educators (single query instead of N queries)
        List<CourseEntity> courses = courseRepository.findByIdIn(courseIds);
        List<Long> educatorIds = courses.stream().map(CourseEntity::getEducatorId).distinct().toList();
        List<Object[]> educatorRows = userRepository.findNicknamesByIds(educatorIds);
        java.util.Map<Long, String> educatorNameMap = educatorRows.stream()
            .collect(java.util.stream.Collectors.toMap(
                row -> (Long) row[0],
                row -> (String) row[1]
            ));

        // 3. Maintain original order
        return courseIds.stream()
            .map(id -> courses.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null))
            .filter(c -> c != null)
            .map(c -> CourseResponse.from(c, educatorNameMap.getOrDefault(c.getEducatorId(), "未知讲师")))
            .toList();
    }
}
