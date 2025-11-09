# Aplicación de Gestión de Consumo de Combustible

## Descripción
Aplicación Android para registrar, visualizar y analizar el consumo de combustible de vehículos personales.

## Funcionalidades Implementadas

### 1. Autenticación (LoginActivity)
- ✅ Inicio de sesión con email/password
- ✅ Registro de nuevos usuarios
- ✅ Inicio de sesión con Google Sign-In
- ✅ Interfaz en español con logo de la aplicación
- ✅ Validaciones de formularios

### 2. Gestión de Vehículos (VehiclesFragment)
- ✅ Listado de vehículos del usuario con RecyclerView
- ✅ Registro de nuevos vehículos (VehicleFormActivity)
- ✅ Edición de vehículos existentes
- ✅ Eliminación de vehículos con confirmación
- ✅ Campos: ID (nickname), Placa, Marca/Modelo, Año, Fecha revisión técnica
- ✅ Generación de código QR para revisión técnica

### 3. Registro de Consumo (RecordsFragment)
- ✅ Listado de registros de combustible con RecyclerView
- ✅ Registro de cargas de combustible (FuelRecordFormActivity)
- ✅ Edición y eliminación de registros
- ✅ Código de 5 dígitos generado automáticamente
- ✅ Campos: Vehículo, Fecha, Litros, Kilometraje, Precio, Tipo combustible
- ✅ Validación: kilometraje mayor al último registro
- ✅ Filtros por vehículo y rango de fechas
- ✅ Tipos de combustible: Gasolina, GLP, GNV

### 4. Resumen y Gráficos (SummaryFragment)
- ✅ Estadísticas: Litros totales, Gasto total, Kilometraje promedio
- ✅ Gráfico de barras: Litros consumidos por mes (últimos 6 meses)
- ✅ Gráfico de torta: Proporción por tipo de combustible
- ✅ Implementado con MPAndroidChart

### 5. Código QR para Revisión Técnica (QRCodeActivity)
- ✅ Generación de código QR con ZXing
- ✅ Contiene: Placa, Último kilometraje, Fecha última revisión
- ✅ Solo visualización en pantalla

### 6. Navegación
- ✅ BottomNavigationView con 3 secciones: Vehículos, Registros, Resumen
- ✅ Navigation Component de Android Jetpack
- ✅ Menú de opciones con botón "Cerrar Sesión"

### 7. Base de Datos
- ✅ Firebase Firestore para almacenamiento
- ✅ Colecciones: `vehicles` y `fuelRecords`
- ✅ Servicios: FirebaseAuthService, FirestoreService
- ✅ Operaciones CRUD completas

### 8. Diseño
- ✅ Material Design 3
- ✅ Colores y estilos personalizados
- ✅ Interfaz completamente en español
- ✅ Responsive y adaptable

## Configuración Necesaria

### 1. Firebase
El proyecto ya tiene el archivo `google-services.json` configurado. Sin embargo, para usar Google Sign-In necesitas:

1. Obtener el Web Client ID de la consola de Firebase
2. Actualizar el string en `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">TU_WEB_CLIENT_ID_AQUI</string>
   ```

Para obtener el Web Client ID:
- Ve a Firebase Console > Project Settings > General
- Busca en "Your apps" el Web Client ID
- O ve a Google Cloud Console > APIs & Services > Credentials

### 2. Reglas de Firestore

Configura las siguientes reglas en Firestore Database:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Regla para vehículos
    match /vehicles/{vehicleId} {
      // Permitir lectura si el usuario es el propietario
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      
      // Permitir creación si el usuario está autenticado y el userId coincide
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      
      // Permitir actualización si el usuario es el propietario
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
      
      // Permitir eliminación si el usuario es el propietario
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    
    // Regla para registros de combustible
    match /fuelRecords/{recordId} {
      // Permitir lectura si el usuario es el propietario
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      
      // Permitir creación si el usuario está autenticado y el userId coincide
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      
      // Permitir actualización si el usuario es el propietario
      allow update: if request.auth != null && resource.data.userId == request.auth.uid;
      
      // Permitir eliminación si el usuario es el propietario
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

### 3. Sincronización de Gradle

Después de clonar/abrir el proyecto:
1. Android Studio te pedirá sincronizar el proyecto con Gradle
2. Acepta la sincronización
3. Espera a que se descarguen todas las dependencias

## Estructura del Proyecto

```
app/src/main/java/com/example/l6_20223209/
├── activities/
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   ├── VehicleFormActivity.java
│   ├── FuelRecordFormActivity.java
│   └── QRCodeActivity.java
├── fragments/
│   ├── VehiclesFragment.java
│   ├── RecordsFragment.java
│   └── SummaryFragment.java
├── adapters/
│   ├── VehicleAdapter.java
│   └── FuelRecordAdapter.java
├── models/
│   ├── Vehicle.java
│   └── FuelRecord.java
└── services/
    ├── FirebaseAuthService.java
    └── FirestoreService.java
```

## Dependencias Principales

- Firebase Auth: Autenticación de usuarios
- Firebase Firestore: Base de datos NoSQL
- Google Sign-In: Autenticación con Google
- Navigation Component: Navegación entre fragmentos
- MPAndroidChart: Gráficos de barras y torta
- ZXing: Generación de códigos QR
- Material Design 3: Componentes de UI

## Compilación y Ejecución

1. Abre el proyecto en Android Studio
2. Sincroniza Gradle
3. Configura el Web Client ID en strings.xml
4. Conecta un dispositivo Android o inicia un emulador
5. Click en "Run" (▶️) o presiona Shift + F10

## Notas Importantes

- **Validación de Kilometraje**: La aplicación valida que cada nuevo registro tenga un kilometraje mayor al último registrado para ese vehículo.
- **Código de Registro**: Se genera automáticamente un código aleatorio de 5 dígitos para cada registro.
- **Gráficos**: Los gráficos se actualizan automáticamente al agregar o eliminar registros.
- **Seguridad**: Todas las operaciones están protegidas por autenticación de Firebase.
- **Offline**: La aplicación requiere conexión a Internet para funcionar correctamente.

## Errores Comunes y Soluciones

1. **Error de compilación con Firebase**: 
   - Verifica que google-services.json esté en app/
   - Sincroniza Gradle nuevamente

2. **Google Sign-In no funciona**:
   - Verifica el Web Client ID en strings.xml
   - Asegúrate de que el SHA-1 esté configurado en Firebase Console

3. **Errores de permisos en Firestore**:
   - Verifica que las reglas de seguridad estén configuradas correctamente
   - Asegúrate de que el usuario esté autenticado

## Autor
LAB6_20223209
Noviembre 2025
