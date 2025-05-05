package com.nastya.booktracker

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BooksViewModel(val dao: BookDao) : ViewModel() {
    private val _navigateToBook = MutableLiveData<Long?>()
    val navigateToBook: LiveData<Long?>
        get() = _navigateToBook

    private val _books = dao.getAll()
    val books: LiveData<List<Book>> = _books

    private val _filteredProducts = MediatorLiveData<List<Book>>()
    val filteredProducts: LiveData<List<Book>> = _filteredProducts

    init {
        _filteredProducts.addSource(_books) { allBooks ->
            _filteredProducts.value = allBooks ?: emptyList()
        }
    }

    fun filterByCategory(category: String) {
        books.value?.let { currentList ->
            _filteredProducts.value = if (category == "all") {
                currentList
            } else {
                currentList.filter { it.category == category }
            }
        }
    }

    fun updateAllCategories() {
        viewModelScope.launch {
            val booksList = dao.getAllOnce()

            val updatedBooks = booksList.map { book ->
                val newCategory = when {
                    book.readPagesCount == 0 -> "want"
                    book.readPagesCount == book.allPagesCount -> "past"
                    else -> "reading"
                }

                if (book.category != newCategory) {
                    book.copy(category = newCategory)
                } else {
                    book
                }
            }

            updatedBooks.forEach { updatedBook ->
                dao.update(updatedBook)
            }
        }
    }


    fun onBookClicked(bookId: Long) {
        _navigateToBook.value = bookId
    }

    fun onBookNavigated() {
        _navigateToBook.value = null
    }

    fun toggleBookIsFavorite(bookId: Long) {
        viewModelScope.launch {
            val book = dao.getNotLive(bookId)
            book!!.isFavorite = !book.isFavorite
            if(book != null) {
                dao.update(book)
            } else {
                Log.e("BooksViewModel", "Книга с bookId=$bookId не найдена.")
            }
        }
    }

//    val initialBooks = listOf(
//        Book(1, "Мастер и Маргарита", "М.А. Булгаков", "reading", "«Мастер и Маргарита» — это смесь мистики, сатиры и вечной истории любви, одно из самых загадочных и захватывающих произведений русской литературы. В Москву 1930-х годов прибывает сам Воланд, дьявол, в сопровождении своей свиты, чтобы внести хаос и справедливость в жизнь советского общества. Параллельно разворачивается трагическая история Мастера, писателя, чей роман о Понтии Пилате отвергли, и его возлюбленной Маргариты, готовой на всё, чтобы спасти его. Готовы ли вы отправиться в путешествие по миру, где стираются границы между реальностью и фантазией, добром и злом?", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 200, 50),
//        Book(2, "Братья Карамазовы", "Ф.М. Достоевский", "past", "Глубокое погружение в моральные, религиозные и философские вопросы человеческого бытия. «Братья Карамазовы» — это многогранная драма о семье, вере, сомнениях и преступлении. История отца и трёх его сыновей, каждый из которых представляет собой разные аспекты человеческой природы: страсть, интеллект и духовность. Убийство отца становится катализатором для исследования самых тёмных уголков человеческой души и поиска смысла жизни.", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 100, 100),
//        Book(3, "Мертвые души", "Н.В. Гоголь", "want", "Путешествие по российской глубинке вместе с Чичиковым, предприимчивым, но сомнительным дельцом, скупающим «мертвые души» — умерших крестьян, числящихся в ревизских сказках. «Мертвые души» — это сатирическое зеркало, отражающее пороки российского общества XIX века: взяточничество, лицемерие, духовную опустошенность и бесхозяйственность. Удастся ли Чичикову провернуть свою аферу и что скрывается за его таинственной целью?", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 300, 0),
//        Book(4, "1984", "Джордж Оруэлл", "want","Погрузитесь в мрачный мир Океании, где Большой Брат следит за каждым вашим шагом, а мысли находятся под строжайшим контролем. «1984» — это пронзительная антиутопия, предостерегающая об опасностях тоталитаризма, манипулирования сознанием и подавления индивидуальности. История Уинстона Смита, стремящегося к свободе в мире, где правда искажена, а любовь запрещена, заставляет задуматься о цене свободы и силе сопротивления.", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 100, 0),
//        Book(5, "Скотный двор", "Джордж Оруэлл", "past", "На ферме «Скотный двор» животные свергают власть людей и устанавливают свои собственные правила, основанные на принципах равенства и справедливости. Однако вскоре идеалы революции оказываются искажены, и власть переходит в руки свиней, которые устанавливают свою тираническую систему. «Скотный двор» — это острая сатирическая притча о природе власти, революции и неизбежности коррупции.", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 40, 40),
//        Book(6, "Портрет Дориана Грея", "Оскар Уайльд", "reading", "В центре сюжета — юный и прекрасный Дориан Грей, чей портрет, написанный талантливым художником, начинает стареть и отражать все его пороки, в то время как сам Дориан остаётся вечно молодым. Одержимый красотой и удовольствиями, он погружается в гедонистическую жизнь, совершая безнравственные поступки, которые не оставляют следа на его лице, но навсегда уродуют его душу. «Портрет Дориана Грея» — это философский роман о цене вечной молодости, моральном разложении и природе красоты.", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 110, 2),
//        Book(7, "Гордость и предубеждение", "Джейн Остин", "want", "", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 200, 0),
//        Book(8, "Грозовой перевал", "Эмили Бронте", "past", "", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 100, 100),
//        Book(9, "Граф Монте-Кристо", "Александр Дюма", "reading", "", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 105, 100),
//        Book(10, "Три мушкетера", "Александр Дюма", "past", "", "https://content.img-gorod.ru/nomenclature/25/885/2588539.jpg?width=304&height=438&fit=bounds", 30, 30),
//    )
}