# Implementation Plan - Fix Blog Creation Error (Malformed JSON)

The user is encountering a `400 Bad Request` with the message `"Malformed JSON request"` when attempting to create a blog post. This is caused by a type mismatch in the `content` field of the request body.

## User Review Required

> [!IMPORTANT]
> The documentation (`blog_api_documentation.md`) currently shows a different structure for the `content` field than what is implemented in the code. I will align the documentation with the code as the code's implementation (structured JSON array) is generally better for REST APIs.

## Proposed Changes

### Blog DTOs

#### [MODIFY] [BlogDtos.kt](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/src/main/kotlin/com/kazemieh/shop/blog/api/dto/BlogDtos.kt)
- Make `content` field optional in `BlogCreateRequest` by providing a default value of `emptyList()`. This allows clients to omit the field if there is no content.
- Note: Even with a default value, sending `"content": ""` will still fail because `""` is a String and cannot be automatically converted to a List. The client must be updated to send `[]` or omit the field.

### Documentation

#### [MODIFY] [blog_api_documentation.md](file:///D:/Android/AndroidStudioProjects/ShopServer/Shop/docs/blog_api_documentation.md)
- Update the `POST /api/admin/blogs` example to show `content` as a JSON array of blocks instead of a stringified JSON.
- Fix the block structure in the documentation to match the `BlogBlock` DTO (using `content` instead of `text`).

## Verification Plan

### Automated Tests
- Since there are no existing controller tests, I will rely on the type system and manual verification instructions for the user.
- I'll check if I can add a simple unit test for the DTO deserialization if I have access to a testing framework.

### Manual Verification
1. Inform the user to change their request body from `"content": ""` to `"content": []` or simply remove the `"content"` field.
2. Verify that the server now accepts the request.
