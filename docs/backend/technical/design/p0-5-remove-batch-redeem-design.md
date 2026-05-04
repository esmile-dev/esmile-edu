# P0-5: Remove Batch Redeem Code Query - Technical Design

## 1. Summary

Remove the batch redeem code list query endpoint (`GET /teacher/redeem-codes`) because:
- The underlying implementation `listCodesByCreator()` ignores the `courseId` parameter (bug)
- `listCodesByCourse()` exists but is never called and also has the same bug
- No front-end or client depends on this endpoint

## 2. API Impact Analysis

### Endpoint Being Removed

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/teacher/redeem-codes` | List redeem codes (paginated) |

**Impact**: Zero - this endpoint is not used by any front-end or external client.

### No Deprecation Period Needed
- The endpoint was never functional (bug: ignores courseId filter)
- No clients depend on it
- Immediate removal is safe

## 3. Files to Modify

### 3.1 RedeemCodeController.java
**Path**: `backend/src/main/java/com/esmile/edu/api/redeem/RedeemCodeController.java`

**Change**: Remove the `listCodes` endpoint (lines 49-54)

```java
// REMOVE THIS ENDPOINT:
@GetMapping("/teacher/redeem-codes")
@RequireRole(Role.TEACHER)
public ApiResponse<Page<RedeemCodeResponse>> listCodes(Pageable pageable) {
    return ApiResponse.ok(redeemBizService.listCodesByCreator(AuthContext.getCurrentUserId(), pageable));
}
```

### 3.2 RedeemBizService.java
**Path**: `backend/src/main/java/com/esmile/edu/biz/RedeemBizService.java`

**Change**: Remove both methods (lines 98-108)

```java
// REMOVE:
@Transactional(readOnly = true)
public Page<RedeemCodeResponse> listCodesByCreator(Long createdBy, Pageable pageable) {
    return redeemCodeRepository.findByCreatedBy(createdBy, pageable)
        .map(RedeemCodeResponse::from);
}

@Transactional(readOnly = true)
public Page<RedeemCodeResponse> listCodesByCourse(Long courseId, Pageable pageable) {
    return redeemCodeRepository.findAll(pageable)
        .map(RedeemCodeResponse::from);
}
```

### 3.3 RedeemCodeRepository.java
**Path**: `backend/src/main/java/com/esmile/edu/module/redeem/RedeemCodeRepository.java`

**Change**: Remove the paginated query method since it's no longer used

```java
// REMOVE:
Page<RedeemCodeEntity> findByCreatedBy(Long createdBy, Pageable pageable);
```

**Keep**: `findByCreatedBy(Long createdBy)` (non-paginated version, may be useful later)

### 3.4 RedeemCodeResponse.java
**Path**: `backend/src/main/java/com/esmile/edu/dto/response/RedeemCodeResponse.java`

**No changes** - This DTO is still used by other responses.

## 4. Test File Updates

### 4.1 RedeemCodeControllerTest.java
**Path**: `backend/src/test/java/com/esmile/edu/api/RedeemCodeControllerTest.java`

**No changes needed** - Tests do not cover the removed `listCodes` endpoint.

### 4.2 RedeemCourseStatusTest.java
**Path**: `backend/src/test/java/com/esmile/edu/api/RedeemCourseStatusTest.java`

**No changes needed** - Tests do not cover the removed `listCodes` endpoint.

## 5. Data Migration

**None required** - This is an API removal only. No database schema changes or data migration needed.

## 6. Summary of Deletions

| File | Lines | What to Remove |
|------|-------|----------------|
| RedeemCodeController.java | 49-54 | `listCodes` endpoint |
| RedeemBizService.java | 98-108 | `listCodesByCreator()` and `listCodesByCourse()` methods |
| RedeemCodeRepository.java | 15 | `findByCreatedBy(Long, Pageable)` method |

## 7. Post-Removal Verification

After removal, verify:
1. Application compiles: `mvn compile -q`
2. RedeemCodeController tests pass: `mvn test -Dtest=RedeemCodeControllerTest`
3. RedeemCourseStatusTest passes: `mvn test -Dtest=RedeemCourseStatusTest`

## 8. Rollback Plan

If issues arise, revert these files from git:
```bash
git checkout HEAD~1 -- backend/src/main/java/com/esmile/edu/api/redeem/RedeemCodeController.java
git checkout HEAD~1 -- backend/src/main/java/com/esmile/edu/biz/RedeemBizService.java
git checkout HEAD~1 -- backend/src/main/java/com/esmile/edu/module/redeem/RedeemCodeRepository.java
```
