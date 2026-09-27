# Свидетельства статического просмотра

Дата подготовки: 27.09.2026. Сборщик 1.0. Исходное приложение не запускалось.

Номера строк относятся к предоставленному снимку. Значения секретов и адрес почты скрыты; наличие строк подтверждено чтением файлов. Выдержки не заменяют анализ всего обработчика.

<a id="e01"></a>
## E01. Доступ к API и аутентификация

`backend/backend/settings.py:111–119`

```text
 111: REST_FRAMEWORK = {
 112:     "DEFAULT_SCHEMA_CLASS": "drf_spectacular.openapi.AutoSchema",
 113:     "DEFAULT_AUTHENTICATION_CLASSES": (
 114:         "rest_framework_simplejwt.authentication.JWTAuthentication",
 115:     ),
 116:     "DEFAULT_PERMISSION_CLASSES": (
 117:         "rest_framework.permissions.AllowAny",
 118:     ),
 119: }
```

`backend/api/views.py:167–186`

```text
 167: def get_patient_from_token(request):
 168:     auth_header = request.META.get('HTTP_AUTHORIZATION', '')
 169:     if auth_header.startswith('Token '):
 170:         token = auth_header.split(' ')[1]
 171:         try:
 172:             return Patient.objects.get(patient_auth_token=token)
 173:         except Patient.DoesNotExist:
 174:             return None
 175:     return None
 176: 
 177: 
 178: class PatientViewSet(viewsets.ModelViewSet):
 179:     queryset = Patient.objects.all()
 180:     serializer_class = serializers.PatientSerializer
 181: 
 182:     http_method_names = ["get", "post", "delete", "patch"]
 183: 
 184:     @action(detail=False, 
 185:         methods=["post"], 
 186:         permission_classes=(permissions.AllowAny,))
```

`backend/api/views.py:254–267`

```text
 254: class DoctorPatientAssignmentViewSet(viewsets.ModelViewSet):
 255:     queryset = DoctorPatientAssignment.objects.all()
 256:     serializer_class = serializers.DoctorPatientAssignmentSerializer
 257: 
 258:     filter_backends = [DjangoFilterBackend]
 259:     filterset_class = filters.DoctorPatientAssignmentFilter
 260: 
 261:     http_method_names = ["get", "post", "delete", "patch"]
 262: 
 263:     def get_queryset(self):
 264:         return DoctorPatientAssignment.objects.filter(assignment_doctor=self.request.user)
 265: 
 266:     def perform_create(self, serializer):
 267:         serializer.save(assignment_doctor=self.request.user)
```

`backend/api/views.py:311–324`

```text
 311: class DocumentViewSet(viewsets.ModelViewSet):
 312:     queryset = Document.objects.all()
 313:     serializer_class = serializers.DocumentSerializer
 314: 
 315:     filter_backends = [DjangoFilterBackend]
 316:     filterset_class = filters.DocumentFilter
 317: 
 318:     http_method_names = ["get", "post", "delete", "patch"]
 319: 
 320:     def create(self, request, *args, **kwargs):
 321:         serializer = self.get_serializer(data=request.data)
 322:         serializer.is_valid(raise_exception=True)
 323:         document_type = serializer.validated_data.get("document_type")
 324:         self.perform_create(serializer)
```

`backend/api/views.py:374–454`

```text
 374: class DiaryViewSet(viewsets.ModelViewSet):
 375:     queryset = Diary.objects.all()
 376:     serializer_class = serializers.DiarySerializer
 377: 
 378:     filter_backends = [DjangoFilterBackend]
 379:     filterset_class = filters.DiaryFilter
 380: 
 381:     http_method_names = ["get", "post"]
 382: 
 383: 
 384: class AnalysisNotesViewSet(viewsets.ReadOnlyModelViewSet):
 385:     queryset = AnalysisNotes.objects.all()
 386:     serializer_class = serializers.AnalysisNotesSerializer
 387:     pagination_class = None
 388: 
 389:     filter_backends = [DjangoFilterBackend]
 390:     filterset_class = filters.AnalyzesFilter
 391: 
 392:     http_method_names = ["get", "post"]
 393: 
 394: 
 395: class NoteViewSet(viewsets.ModelViewSet):
 396:     queryset = Note.objects.all()
 397:     serializer_class = serializers.NoteSerializer
 398: 
 399:     filter_backends = [DjangoFilterBackend]
 400:     filterset_class = filters.NoteFilter
 401: 
 402:     http_method_names = ["get", "post"]
 403: 
 404:     def perform_create(self, serializer):
 405:         serializer.save(note_doctor=self.request.user)
 406: 
 407: 
 408: 
 409: class DeviceDataViewSet(viewsets.ModelViewSet):
 410:     queryset = DeviceData.objects.all()
 411:     serializer_class = serializers.DeviceDataSerializer
 412: 
 413:     filter_backends = [DjangoFilterBackend]
 414:     filterset_class = filters.DeviceDataFilter
 415: 
 416:     http_method_names = ["get", "post"]
 417: 
 418: 
 419: class NotificationViewSet(viewsets.ModelViewSet):
 420:     queryset = Notification.objects.all()
 421:     serializer_class = serializers.NotificationSerializer
 422:     # permission_classes = [permissions.IsAuthenticated]
 423:     http_method_names = ["get", "patch"]
 424: 
 425: 
 426:     @action(detail=False, methods=["patch"])
 427:     def mark_all_read(self, request):
 428:         Notification.objects.filter(
 429:             notification_doctor=request.user,
 430:             notification_is_read=False
 431:         ).update(notification_is_read=True)
 432:         return Response({"detail": "Все уведомления отмечены как прочитанные"})
 433: 
 434:     @action(detail=False, methods=["get"])
 435:     def unread_count(self, request):
 436:         count = Notification.objects.filter(
 437:             notification_doctor=request.user,
 438:             notification_is_read=False
 439:         ).count()
 440:         return Response({"count": count})
 441: 
 442:     def perform_update(self, serializer):
 443:         serializer.save(notification_is_read=True)
 444: 
 445: 
 446: class AppointmentViewSet(viewsets.ModelViewSet):
 447:     queryset = Appointment.objects.all()
 448:     serializer_class = serializers.AppointmentSerializer
 449:     pagination_class = None
 450: 
 451:     filter_backends = [DjangoFilterBackend]
 452:     filterset_class = filters.AppointmentFilter
 453: 
 454:     http_method_names = ["get", "post", "delete"]
```

`backend/api/urls.py:1–31`

```text
   1: from django.urls import path, include
   2: from rest_framework.routers import SimpleRouter
   3: from api import views
   4: from rest_framework_simplejwt.views import TokenRefreshView
   5: from .serializers import DoctorTokenView
   6: 
   7: router = SimpleRouter()
   8: 
   9: router.register("specializations", views.SpecializationViewSet)
  10: router.register("chronicdiseas", views.ChronicDiseasViewSet)
  11: router.register("analysisindicators", views.AnalysisIndicatorsViewSet)
  12: 
  13: router.register("analysisnotes", views.AnalysisNotesViewSet)
  14: 
  15: router.register("doctors", views.DoctorViewSet)
  16: router.register("patients", views.PatientViewSet)
  17: router.register("assignments", views.DoctorPatientAssignmentViewSet)
  18: 
  19: router.register("diaries", views.DiaryViewSet)
  20: router.register("documents", views.DocumentViewSet)
  21: router.register("notes", views.NoteViewSet)
  22: router.register("devicedata", views.DeviceDataViewSet)
  23: 
  24: router.register("notifications", views.NotificationViewSet)
  25: router.register("appointments", views.AppointmentViewSet)
  26: 
  27: urlpatterns = [
  28:     path("", include(router.urls)),
  29:     path('token/', DoctorTokenView.as_view(), name='token_obtain_pair'),
  30:     path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
  31: ]
```

`HealthMonitor/app/src/main/java/com/example/healthmonitor/data/auth/AuthManager.kt:32–36`

```text
  32:     fun logout() {
  33:         prefs.edit().clear().apply()
  34:     }
  35: 
  36:     fun getAuthHeader(): String? = token?.let { "Token $it" }
```

<a id="e02"></a>
## E02. Поля пациента

`backend/api/serializers.py:119–133`

```text
 119: class PatientSerializer(serializers.ModelSerializer):
 120:     patient_diagnosis = ChronicDiseasSerializer(required=False, many=True)
 121: 
 122:     class Meta:
 123:         model = Patient
 124:         fields = "__all__"
 125: 
 126: 
 127: class DoctorPatientAssignmentSerializer(serializers.ModelSerializer):
 128:     assignment_patient = PatientSerializer()
 129: 
 130:     class Meta:
 131:         model = DoctorPatientAssignment
 132:         fields = ("id", "assignment_doctor", "assignment_patient", "assignment_status", "assignment_date")
 133:         read_only_fields = ("assignment_doctor", "assignment_date")
```

`backend/api/models.py:261–292`

```text
 261:     patient_password = models.CharField(
 262:         max_length=250,
 263:         null=False,
 264:         blank=False,
 265:         validators=[validate_password],
 266:         verbose_name="Пароль",
 267:     )
 268:     patient_diagnosis = models.ManyToManyField(
 269:         ChronicDiseas,
 270:         null=True,
 271:         blank=True,
 272:         verbose_name="Диагнозы"
 273:     )
 274:     patient_last_update = models.DateTimeField(
 275:         null=True,
 276:         blank=True,
 277:         verbose_name="Последнее обновление данных"
 278:     )
 279:     patient_auth_token = models.CharField(
 280:         max_length=64, 
 281:         null=True, 
 282:         blank=True, 
 283:         unique=True,
 284:         verbose_name="Токен авторизации"
 285:     )
 286:     patient_invite_code = models.CharField(
 287:         max_length=6, 
 288:         unique=True, 
 289:         null=True, 
 290:         blank=True,
 291:         verbose_name="Код привязки"
 292:     )
```

<a id="e03"></a>
## E03. Секреты и настройки (значения скрыты)

`backend/backend/settings.py:9–13`

```text
   9: SECRET_KEY = [REDACTED]
  10: 
  11: DEBUG = True
  12: 
  13: ALLOWED_HOSTS = ["192.168.1.175", "192.168.1.175:8000", "192.168.34.106", "192.168.34.106:8000", "127.0.0.1", "*"]
```

`backend/backend/settings.py:66–75`

```text
  66: DATABASES = {
  67:     "default": {
  68:         "ENGINE": "django.db.backends.postgresql",
  69:         "NAME": "postgres",
  70:         "USER": "postgres",
  71:         "PASSWORD": [REDACTED]
  72:         "HOST": "localhost",
  73:         "PORT": "5432",
  74:     }
  75: }
```

`backend/backend/settings.py:138–147`

```text
 138: CORS_ALLOWED_ORIGINS = ["http://localhost:3000"]
 139: FRONTEND_URL = 'http://localhost:3000'
 140: 
 141: EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
 142: EMAIL_HOST = 'smtp.yandex.ru'
 143: EMAIL_PORT = 465
 144: EMAIL_USE_SSL = True 
 145: EMAIL_HOST_USER = [REDACTED]
 146: EMAIL_HOST_PASSWORD = [REDACTED]
 147: DEFAULT_FROM_EMAIL = [REDACTED]
```

`docker-compose.yml:1–38`

```text
   1: services:
   2:   db:
   3:     image: postgres:15
   4:     volumes:
   5:       - postgres_data:/var/lib/postgresql/data
   6:     environment:
   7:       POSTGRES_DB: healthhelp
   8:       POSTGRES_USER: healthuser
   9:       POSTGRES_PASSWORD: [REDACTED]
  10:     ports:
  11:       - "5432:5432"
  12:     healthcheck:
  13:       test: ["CMD-SHELL", "pg_isready -U healthuser -d healthhelp"]
  14:       interval: 5s
  15:       timeout: 5s
  16:       retries: 5
  17: 
  18:   backend:
  19:     build: ./backend
  20:     volumes:
  21:       - ./backend:/app
  22:       - static_volume:/app/staticfiles
  23:       - media_volume:/app/media
  24:     ports:
  25:       - "8000:8000"
  26:     depends_on:
  27:       db:
  28:         condition: service_healthy
  29:     environment:
  30:       - DB_NAME=healthhelp
  31:       - DB_USER=healthuser
  32:       - DB_PASSWORD= [REDACTED]
  33:       - DB_HOST=db
  34:       - DB_PORT=5432
  35:       - SECRET_KEY= [REDACTED]
  36:       - DEBUG=True
  37:       - ALLOWED_HOSTS=*
  38:     working_dir: /app
```

<a id="e04"></a>
## E04. HTTP и журналирование

`HealthMonitor/app/src/main/java/com/example/healthmonitor/data/network/NetworkModule.kt:13–24`

```text
  13: object NetworkModule {
  14: 
  15:     private const val BASE_URL = "http://192.168.1.175:8000/"
  16: 
  17:     fun init(appContext: Context) {
  18:         AuthManager.init(appContext)
  19:     }
  20: 
  21:     val api: HealthApi by lazy {
  22:         val logging = HttpLoggingInterceptor().apply {
  23:             level = HttpLoggingInterceptor.Level.BODY
  24:         }
```

`HealthMonitor/app/src/main/AndroidManifest.xml:6–15`

```text
   6:     <application
   7:         android:name=".MyApplication"
   8:         android:allowBackup="true"
   9:         android:icon="@mipmap/ic_launcher"
  10:         android:label="Health Monitor"
  11:         android:roundIcon="@mipmap/ic_launcher_round"
  12:         android:supportsRtl="true"
  13:         android:theme="@android:style/Theme.Material.Light.NoActionBar"
  14:         android:networkSecurityConfig="@xml/network_security_config"
  15:         android:usesCleartextTraffic="true">
```

`HealthMonitor/app/src/main/res/xml/network_security_config.xml:1–8`

```text
   1: <?xml version="1.0" encoding="utf-8"?>
   2: <network-security-config>
   3:     <base-config cleartextTrafficPermitted="true">
   4:         <trust-anchors>
   5:             <certificates src="system" />
   6:         </trust-anchors>
   7:     </base-config>
   8: </network-security-config>
```

`backend/api/views.py:339–350`

```text
 339:                 def _run_parse():
 340:                     tid = threading.current_thread().name
 341:                     sys.stdout.flush()
 342:                     try:
 343:                         result = parse_medical_pdf(tmp_path)
 344:                         tests = result.get("tests", [])
 345:                         report_date = result.get("date")
 346:                         print(
 347:                             json.dumps(result, ensure_ascii=False, indent=2), flush=True
 348:                         )
 349:                         save_analysis_notes(patient, report_date, tests)
 350:                         sys.stdout.flush()
```

`frontend/src/LoginPage/LoginPage.js:12–32`

```text
  12:   const handleLogin = async (e) => {
  13:     e.preventDefault();
  14:     setError('');
  15:     setLoading(true);
  16: 
  17:     try {
  18:       const response = await fetch('http://127.0.0.1:8000/api/token/', {
  19:         method: 'POST',
  20:         headers: { 'Content-Type': 'application/json' },
  21:         body: JSON.stringify({ username, password }),
  22:       });
  23: 
  24:       if (!response.ok) {
  25:         throw new Error('Неверный логин или пароль');
  26:       }
  27: 
  28:       const data = await response.json();
  29: 
  30:       localStorage.setItem('accessToken', data.access);
  31:       localStorage.setItem('refreshToken', data.refresh);
  32:       localStorage.setItem('doctor', JSON.stringify(data.doctor));
```

<a id="e05"></a>
## E05. Файлы и LLM

`backend/api/serializers.py:136–168`

```text
 136: class DocumentSerializer(serializers.ModelSerializer):
 137:     class Meta:
 138:         model = Document
 139:         fields = [
 140:             "id",
 141:             "document_patient",
 142:             "document_type",
 143:             "document_file",
 144:             "document_name",
 145:             "document_date",
 146:         ]
 147:     
 148: 
 149: 
 150: class DiarySerializer(serializers.ModelSerializer):
 151:     class Meta:
 152:         model = Diary
 153:         fields = "__all__"
 154: 
 155: 
 156: class DocumentParseSerializer(serializers.Serializer):
 157:     file = serializers.FileField(required=True, allow_empty_file=False)
 158:     document_type = serializers.ChoiceField(choices=TypeEnum.choices, required=True)
 159:     document_name = serializers.CharField(
 160:         required=False, allow_blank=True, max_length=255
 161:     )
 162: 
 163:     def validate_file(self, value):
 164:         if not value.name.lower().endswith(".pdf"):
 165:             raise serializers.ValidationError("Только PDF файлы поддерживаются")
 166:         if value.size > 25 * 1024 * 1024:
 167:             raise serializers.ValidationError("Файл слишком большой (макс. 25 MB)")
 168:         return value
```

`backend/api/views.py:270–308`

```text
 270: def normalize_date(date_str) :
 271:     if not date_str or not isinstance(date_str, str):
 272:         return timezone.now()
 273:     clean_str = re.sub(r"[^\d\-\.\/]", "", date_str).strip()
 274:     for fmt in ("%Y-%m-%d", "%d.%m.%Y", "%d-%m-%Y", "%Y/%m/%d"):
 275:         try:
 276:             return datetime.strptime(clean_str, fmt)
 277:         except ValueError:
 278:             continue
 279: 
 280:     return datetime.timezone.now()
 281: 
 282: 
 283: def save_analysis_notes(patient_instance, report_date, tests_data):
 284:     target_date = normalize_date(report_date)
 285:     for test in tests_data:
 286:         raw_name = test.get("name", "").strip()
 287:         if not raw_name:
 288:             continue
 289:         clean_name = re.sub(r"\s*[\(\[].*?[\)\]]", "", raw_name).strip()
 290:         indicator = AnalysisIndicators.objects.filter(
 291:             analysis_indicators_name=clean_name
 292:         ).first()
 293:         if indicator is None:
 294:             continue
 295:         raw_value = str(test.get("value", "0")).replace(",", ".").strip()
 296:         try:
 297:             value_float = float(raw_value)
 298:         except ValueError:
 299:             continue
 300: 
 301:         measure = test.get("unit", "").strip()
 302:         AnalysisNotes.objects.create(
 303:             notes_patient=patient_instance,
 304:             notes_indicators=indicator,
 305:             notes_value=value_float,
 306:             notes_measure=measure,
 307:             notes_date=target_date,
 308:         )
```

`backend/api/views.py:320–363`

```text
 320:     def create(self, request, *args, **kwargs):
 321:         serializer = self.get_serializer(data=request.data)
 322:         serializer.is_valid(raise_exception=True)
 323:         document_type = serializer.validated_data.get("document_type")
 324:         self.perform_create(serializer)
 325: 
 326:         patient = serializer.validated_data.get("document_patient")
 327:         if str(document_type).lower() == str(TypeEnum.ANALYZES).lower():
 328:             uploaded_file = serializer.validated_data.get("document_file")
 329:             if uploaded_file:
 330:                 with tempfile.NamedTemporaryFile(
 331:                     suffix=".pdf", delete=False
 332:                 ) as tmp_file:
 333:                     tmp_path = tmp_file.name
 334:                     uploaded_file.seek(0)
 335:                     for chunk in uploaded_file.chunks():
 336:                         tmp_file.write(chunk)
 337:                 doc_id = serializer.instance.id
 338: 
 339:                 def _run_parse():
 340:                     tid = threading.current_thread().name
 341:                     sys.stdout.flush()
 342:                     try:
 343:                         result = parse_medical_pdf(tmp_path)
 344:                         tests = result.get("tests", [])
 345:                         report_date = result.get("date")
 346:                         print(
 347:                             json.dumps(result, ensure_ascii=False, indent=2), flush=True
 348:                         )
 349:                         save_analysis_notes(patient, report_date, tests)
 350:                         sys.stdout.flush()
 351:                     except Exception as e:
 352:                         import traceback
 353: 
 354:                         traceback.print_exc(file=sys.stdout)
 355:                         sys.stdout.flush()
 356:                     finally:
 357:                         if os.path.exists(tmp_path):
 358:                             os.unlink(tmp_path)
 359: 
 360:                 thread = threading.Thread(
 361:                     target=_run_parse, daemon=True, name=f"parse-{doc_id}"
 362:                 )
 363:                 thread.start()
```

`backend/api/function/conv.py:40–79`

```text
  40: def extract_raw_text(pdf_path):
  41:     with pdfplumber.open(pdf_path) as pdf:
  42:         pages_text = []
  43:         for _, page in enumerate(pdf.pages):
  44:             text = page.extract_text()
  45:             pages_text.append(text)
  46:         result = "\n".join(pages_text)
  47:     return result
  48: 
  49: 
  50: def parse_with_llm(raw_text):
  51:     try:
  52:         response = client.chat.completions.create(
  53:             model="qwen2.5:7b",
  54:             messages=[
  55:                 {"role": "system", "content": SYSTEM_PROMPT},
  56:                 {"role": "user", "content": USER_PROMPT.format(text=raw_text[:4000])},
  57:             ],
  58:             temperature=0.0,
  59:             max_tokens=1500,
  60:             response_format={"type": "json_object"},
  61:         )
  62:         raw_json = response.choices[0].message.content
  63:         fixed_json = repair_json(raw_json)
  64:         return json.loads(fixed_json)
  65:     except Exception as e:
  66:         raise
  67: 
  68: 
  69: def parse_medical_pdf(pdf_path: str) -> dict:
  70:     raw_text = extract_raw_text(pdf_path)
  71: 
  72:     if not raw_text.strip():
  73:         return {"tests": []}
  74: 
  75:     result = parse_with_llm(raw_text)
  76: 
  77:     if not isinstance(result, dict) or "tests" not in result:
  78:         raise ValueError("LLM вернула ответ без ключа 'tests'")
  79:     return result
```

`backend/backend/urls.py:17–24`

```text
  17: if settings.DEBUG:
  18:     from drf_spectacular.views import (
  19:         SpectacularAPIView,
  20:         SpectacularRedocView,
  21:         SpectacularSwaggerView,
  22:     )
  23: 
  24:     urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
```

`frontend/nginx.conf:1–20`

```text
   1: server {
   2:     listen 80;
   3:     
   4:     root /usr/share/nginx/html;
   5:     index index.html;
   6: 
   7:     location / {
   8:         try_files $uri $uri/ /index.html;
   9:     }
  10: 
  11:     location /api/ {
  12:         proxy_pass http://backend:8000;
  13:         proxy_set_header Host $host;
  14:         proxy_set_header X-Real-IP $remote_addr;
  15:     }
  16: 
  17:     location /media/ {
  18:         alias /usr/share/nginx/html/media/;
  19:     }
  20: }
```

<a id="e06"></a>
## E06. Сборка и зависимости

`backend/Dockerfile:1–30`

```text
   1: FROM python:3.10
   2: 
   3: RUN apt-get update && apt-get install -y \
   4:     postgresql-client \
   5:     libpq-dev \
   6:     gcc \
   7:     curl \
   8:     && rm -rf /var/lib/apt/lists/*
   9: 
  10: RUN pip install --no-cache-dir poetry
  11: 
  12: WORKDIR /app
  13: 
  14: COPY pyproject.toml poetry.lock ./
  15: 
  16: RUN poetry config virtualenvs.create false \
  17:     && poetry install --no-interaction --no-ansi --no-root
  18: 
  19: RUN pip install --no-cache-dir gunicorn
  20: 
  21: COPY . .
  22: 
  23: RUN python manage.py collectstatic --noinput || true
  24: 
  25: COPY docker-cmd.sh /docker-cmd.sh
  26: RUN chmod +x /docker-cmd.sh
  27: 
  28: EXPOSE 8000
  29: 
  30: ENTRYPOINT ["/docker-cmd.sh"]
```

`backend/docker-cmd.sh:1–13`

```text
   1: #!/bin/bash
   2: set -e
   3: 
   4: echo "Выполнение миграций"
   5: python manage.py migrate --noinput
   6: 
   7: echo "Импорт дефолтных данных"
   8: python manage.py import_analysis_indicators
   9: python manage.py import_chronic_diseases
  10: python manage.py import_specializations
  11: 
  12: echo "Запуск сервера..."
  13: exec gunicorn --bind 0.0.0.0:8000 --workers 3 --timeout 120 backend.wsgi:application
```

`frontend/Dockerfile:1–18`

```text
   1: FROM node:18-alpine AS build
   2: 
   3: WORKDIR /app
   4: 
   5: COPY package*.json ./
   6: RUN npm install
   7: 
   8: COPY . .
   9: RUN npm run build
  10: 
  11: FROM nginx:alpine
  12: 
  13: COPY --from=build /app/build /usr/share/nginx/html
  14: COPY nginx.conf /etc/nginx/conf.d/default.conf
  15: 
  16: EXPOSE 80
  17: 
  18: CMD ["nginx", "-g", "daemon off;"]
```

`backend/api/function/conv.py:7–11`

```text
   7: client = OpenAI(
   8:     base_url="http://localhost:11434/v1",
   9:     api_key="ollama",
  10:     timeout=Timeout(timeout=120.0, connect=10.0),
  11: )
```

<a id="e07"></a>
## E07. Имеющиеся тесты

`frontend/src/App.test.js:1–8`

```text
   1: import { render, screen } from '@testing-library/react';
   2: import App from './App';
   3: 
   4: test('renders learn react link', () => {
   5:   render(<App />);
   6:   const linkElement = screen.getByText(/learn react/i);
   7:   expect(linkElement).toBeInTheDocument();
   8: });
```

`HealthMonitor/app/src/test/java/com/example/healthmonitor/ExampleUnitTest.kt:1–17`

```text
   1: package com.example.healthmonitor
   2: 
   3: import org.junit.Test
   4: 
   5: import org.junit.Assert.*
   6: 
   7: /**
   8:  * Example local unit test, which will execute on the development machine (host).
   9:  *
  10:  * See [testing documentation](http://d.android.com/tools/testing).
  11:  */
  12: class ExampleUnitTest {
  13:     @Test
  14:     fun addition_isCorrect() {
  15:         assertEquals(4, 2 + 2)
  16:     }
  17: }
```

`HealthMonitor/app/src/androidTest/java/com/example/healthmonitor/ExampleInstrumentedTest.kt:1–24`

```text
   1: package com.example.healthmonitor
   2: 
   3: import androidx.test.platform.app.InstrumentationRegistry
   4: import androidx.test.ext.junit.runners.AndroidJUnit4
   5: 
   6: import org.junit.Test
   7: import org.junit.runner.RunWith
   8: 
   9: import org.junit.Assert.*
  10: 
  11: /**
  12:  * Instrumented test, which will execute on an Android device.
  13:  *
  14:  * See [testing documentation](http://d.android.com/tools/testing).
  15:  */
  16: @RunWith(AndroidJUnit4::class)
  17: class ExampleInstrumentedTest {
  18:     @Test
  19:     fun useAppContext() {
  20:         // Context of the app under test.
  21:         val appContext = InstrumentationRegistry.getInstrumentation().targetContext
  22:         assertEquals("com.example.healthmonitor", appContext.packageName)
  23:     }
  24: }
```

<a id="e08"></a>
## E08. Показатели и прототип

`HealthMonitor/app/src/main/java/com/example/healthmonitor/viewmodel/MainViewModel.kt:330–345`

```text
 330:     private fun loadVitalsFromLocal() {
 331:         viewModelScope.launch {
 332:             val patientId = getPatientId() ?: return@launch
 333:             _uiState.value = _uiState.value.copy(
 334:                 vitals = db.vitalDao().getRecent(patientId)
 335:             )
 336:         }
 337:     }
 338: 
 339:     fun refreshMockData() {
 340:         viewModelScope.launch {
 341:             val patientId = getPatientId() ?: return@launch
 342:             val mockVital = MockGenerator.generateVital().copy(patientId = patientId)
 343:             db.vitalDao().insert(mockVital)
 344:             loadVitalsFromLocal()
 345:         }
```

`backend/api/validators.py:48–52`

```text
  48: def validate_mark_device(value):
  49:     if value < 0 and value > 100:
  50:         raise ValidationError(
  51:             f"Оценка не может быть больше 100 или меньше 0."
  52:         )
```

`backend/api/models.py:297–301`

```text
 297:     def generate_invite_code(self):
 298:         while True:
 299:             code = ''.join(random.choices(string.digits, k=6))
 300:             if not Patient.objects.filter(patient_invite_code=code).exists():
 301:                 return code
```
