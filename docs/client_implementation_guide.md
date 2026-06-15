# Client-Side Implementation Guide: Rich Text Editor (Compose Multiplatform)

To implement a professional WordPress-like editor in your KMP app, we will use a **Block-based Architecture**. This guide explains how to build the editor and renderer.

## 1. Data Structure (The "Block" Model)
Define a sealed class to represent different types of content blocks. This matches the JSON structure stored on the server.

```kotlin
@Serializable
sealed class BlogBlock {
    @Serializable @SerialName("paragraph")
    data class Paragraph(val text: String) : BlogBlock()
    
    @Serializable @SerialName("image")
    data class Image(val url: String, val caption: String? = null) : BlogBlock()
    
    @Serializable @SerialName("header")
    data class Header(val text: String, val level: Int) : BlogBlock()
    
    @Serializable @SerialName("list")
    data class BulletList(val items: List<String>) : BlogBlock()
    
    @Serializable @SerialName("table")
    data class Table(val rows: List<List<String>>) : BlogBlock()
}

@Serializable
data class BlogContent(val blocks: List<BlogBlock>)
```

## 2. The Editor (Admin Side)
For the editor, you need a dynamic list where users can add/remove/reorder blocks.

### Key Logic:
- **Image Upload:** When a user adds an image, first call `POST /api/admin/blogs/media/upload`. Take the returned `url` and create an `Image` block.
- **State Management:** Use a `MutableStateList<BlogBlock>` to keep track of blocks in the UI.
- **Serialization:** When saving, use `Json.encodeToString(BlogContent(blocks))` to create the JSON string for the server.

### UI Example (Pseudo-code):
```kotlin
LazyColumn {
    items(blocks) { block ->
        when (block) {
            is BlogBlock.Paragraph -> BasicTextField(value = block.text, ...)
            is BlogBlock.Header -> HeaderField(block)
            is BlogBlock.Image -> AsyncImage(model = block.url, ...)
        }
    }
    item {
        AddBlockButtons(onAdd = { newBlock -> blocks.add(newBlock) })
    }
}
```

## 3. The Renderer (User Side)
This is where you display the blog post in the mobile app.

```kotlin
@Composable
fun BlogRenderer(content: BlogContent) {
    SelectionContainer {
        Column(modifier = Modifier.padding(16.dp)) {
            content.blocks.forEach { block ->
                when (block) {
                    is BlogBlock.Paragraph -> Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    is BlogBlock.Header -> Text(
                        text = block.text,
                        style = when(block.level) {
                            1 -> MaterialTheme.typography.headlineLarge
                            else -> MaterialTheme.typography.headlineMedium
                        }
                    )
                    is BlogBlock.Image -> {
                        AsyncImage(model = block.url, contentDescription = block.caption)
                        block.caption?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                    is BlogBlock.Table -> BlogTable(block.rows)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
```

## 4. Recommended Libraries
- **Ktor:** For API calls.
- **Kotlinx Serialization:** For converting the block list to/from JSON.
- **Coil3:** For image loading in Compose Multiplatform.
- **RichText (Optional):** For complex spans (bold, italic) within a paragraph block, consider `com.halilibo.richtext:richtext-ui-material3`.

## 5. Summary Flow
1. **Fetch:** `GET /api/blogs/{slug}` returns a JSON string in the `content` field.
2. **Parse:** Client uses `Json.decodeFromString<BlogContent>(response.content)`.
3. **Render:** `BlogRenderer(parsedContent)` displays the UI natively.
